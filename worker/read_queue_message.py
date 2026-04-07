#!/usr/bin/env python3
import json
import uuid
from pathlib import Path
from typing import Any

import pika
import psycopg
from botocore.client import Config
import boto3

RABBITMQ_HOST = "localhost"
RABBITMQ_PORT = 5672
RABBITMQ_USERNAME = "guest"
RABBITMQ_PASSWORD = "guest"
RABBITMQ_VHOST = "/"
QUEUE_NAME = "media.processing"
AUTO_ACK = False

DB_HOST = "localhost"
DB_PORT = 35432
DB_NAME = "jbof"
DB_USER = "user"
DB_PASSWORD = "password"

MINIO_ENDPOINT = "http://localhost:9002"
MINIO_ACCESS_KEY = "minio"
MINIO_SECRET_KEY = "minio123"
MINIO_REGION = "us-east-1"

DOWNLOAD_DIR = Path(__file__).resolve().parent / "downloads"


def parse_body(body: bytes) -> Any:
    return json.loads(body.decode("utf-8"))


def fetch_media_row(media_id: str, media_type: str) -> dict[str, Any] | None:
    media_uuid = uuid.UUID(media_id)
    with psycopg.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
    ) as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                select media_file_id, kind, bucket, object_key, original_filename, content_type, size_bytes
                from media_files
                where media_file_id = %s
                """,
                (media_uuid,),
            )
            row = cur.fetchone()
            if row is None:
                return None

            record = {
                "media_file_id": str(row[0]),
                "kind": row[1],
                "bucket": row[2],
                "object_key": row[3],
                "original_filename": row[4],
                "content_type": row[5],
                "size_bytes": row[6],
            }
            if record["kind"] != media_type:
                raise ValueError(
                    f"Type mismatch. Message type={media_type}, DB kind={record['kind']}"
                )
            return record


def download_from_minio(bucket: str, object_key: str, file_name: str) -> Path:
    s3 = boto3.client(
        "s3",
        endpoint_url=MINIO_ENDPOINT,
        aws_access_key_id=MINIO_ACCESS_KEY,
        aws_secret_access_key=MINIO_SECRET_KEY,
        region_name=MINIO_REGION,
        config=Config(s3={"addressing_style": "path"}),
    )

    DOWNLOAD_DIR.mkdir(exist_ok=True)
    target_path = DOWNLOAD_DIR / file_name

    response = s3.get_object(Bucket=bucket, Key=object_key)
    body = response["Body"].read()
    target_path.write_bytes(body)
    return target_path


def process_message(payload: dict[str, Any]) -> None:
    media_id = payload.get("id")
    media_type = payload.get("type")
    if not media_id or not media_type:
        raise ValueError("Message must include 'id' and 'type'")

    record = fetch_media_row(media_id, media_type)
    if record is None:
        raise ValueError(f"No media_files row found for id={media_id}")

    safe_name = record["original_filename"] or f"{record['media_file_id']}.bin"
    downloaded = download_from_minio(
        bucket=record["bucket"],
        object_key=record["object_key"],
        file_name=f"{record['media_file_id']}_{safe_name}",
    )

    print("DB row:")
    print(json.dumps(record, indent=2))
    print(f"Downloaded file: {downloaded}")


def main() -> None:
    credentials = pika.PlainCredentials(RABBITMQ_USERNAME, RABBITMQ_PASSWORD)
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            port=RABBITMQ_PORT,
            virtual_host=RABBITMQ_VHOST,
            credentials=credentials,
        )
    )
    channel = connection.channel()
    channel.queue_declare(queue=QUEUE_NAME, durable=True)
    channel.basic_qos(prefetch_count=1)

    print(f"Listening on queue '{QUEUE_NAME}' (Ctrl+C to stop)...")

    def on_message(ch, method, _, body: bytes) -> None:
        try:
            payload = parse_body(body)
            print("Received message:")
            print(json.dumps(payload, indent=2))
            process_message(payload)
            if not AUTO_ACK:
                ch.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as exc:
            print(f"Failed processing message: {exc}")

    channel.basic_consume(queue=QUEUE_NAME, on_message_callback=on_message, auto_ack=AUTO_ACK)

    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        pass
    finally:
        if connection.is_open:
            connection.close()


if __name__ == "__main__":
    main()
