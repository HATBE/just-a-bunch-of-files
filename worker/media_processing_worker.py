#!/usr/bin/env python3
from __future__ import annotations

import io
import json
import logging
import os
import sys
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone
from decimal import Decimal
from typing import Any

import boto3
import pika
import psycopg
from PIL import ExifTags, Image, ImageOps
from botocore.client import Config
from psycopg.rows import dict_row
from psycopg.types.json import Json


LOGGER = logging.getLogger("media_processing_worker")


@dataclass(frozen=True)
class RabbitMqSettings:
    host: str
    port: int
    username: str
    password: str
    virtual_host: str
    queue_name: str

    @classmethod
    def from_env(cls) -> "RabbitMqSettings":
        return cls(
            host=os.getenv("SPRING_RABBITMQ_HOST", "localhost"),
            port=int(os.getenv("SPRING_RABBITMQ_PORT", "5672")),
            username=os.getenv("SPRING_RABBITMQ_USERNAME", "guest"),
            password=os.getenv("SPRING_RABBITMQ_PASSWORD", "guest"),
            virtual_host=os.getenv("SPRING_RABBITMQ_VIRTUAL_HOST", "/"),
            queue_name=os.getenv("APP_RABBITMQ_MEDIA_PROCESSING_QUEUE", "media.processing"),
        )


@dataclass(frozen=True)
class DatabaseSettings:
    dsn: str

    @classmethod
    def from_env(cls) -> "DatabaseSettings":
        host = os.getenv("WORKER_DB_HOST", "localhost")
        port = os.getenv("WORKER_DB_PORT", "35432")
        dbname = os.getenv("WORKER_DB_NAME", "jbof")
        user = os.getenv("WORKER_DB_USER", "user")
        password = os.getenv("WORKER_DB_PASSWORD", "password")
        return cls(
            dsn=f"host={host} port={port} dbname={dbname} user={user} password={password}"
        )


@dataclass(frozen=True)
class ObjectStorageSettings:
    endpoint: str
    access_key: str
    secret_key: str
    region: str
    path_style: bool
    thumbnail_bucket: str

    @classmethod
    def from_env(cls) -> "ObjectStorageSettings":
        return cls(
            endpoint=os.getenv("STORAGE_S3_ENDPOINT", "http://localhost:9002"),
            access_key=os.getenv("STORAGE_S3_ACCESS_KEY", "minio"),
            secret_key=os.getenv("STORAGE_S3_SECRET_KEY", "minio123"),
            region=os.getenv("STORAGE_S3_REGION", "us-east-1"),
            path_style=os.getenv("STORAGE_S3_PATH_STYLE", "true").lower() == "true",
            thumbnail_bucket=os.getenv("WORKER_THUMBNAIL_BUCKET", "images"),
        )


@dataclass(frozen=True)
class WorkerSettings:
    rabbitmq: RabbitMqSettings
    database: DatabaseSettings
    object_storage: ObjectStorageSettings

    @classmethod
    def from_env(cls) -> "WorkerSettings":
        return cls(
            rabbitmq=RabbitMqSettings.from_env(),
            database=DatabaseSettings.from_env(),
            object_storage=ObjectStorageSettings.from_env(),
        )


@dataclass(frozen=True)
class MediaFileQueueMessage:
    media_file_id: uuid.UUID
    created_at: datetime | None

    @classmethod
    def from_body(cls, body: bytes) -> "MediaFileQueueMessage":
        payload = json.loads(body.decode("utf-8"))
        return cls(
            media_file_id=uuid.UUID(payload["mediaFileId"]),
            created_at=parse_datetime(payload.get("createdAt")),
        )


@dataclass(frozen=True)
class MediaFileRecord:
    media_file_id: uuid.UUID
    kind: str
    bucket: str
    object_key: str
    original_filename: str
    content_type: str
    processing_status: str
    metadata: dict[str, Any]


@dataclass(frozen=True)
class ExtractedMetadata:
    width: int | None = None
    height: int | None = None
    captured_at: datetime | None = None
    camera_make: str | None = None
    camera_model: str | None = None
    gps_lat: float | None = None
    gps_lon: float | None = None
    metadata_json: dict[str, Any] | None = None


@dataclass(frozen=True)
class ThumbnailResult:
    bucket: str
    object_key: str
    width: int
    height: int
    size_bytes: int


class DatabaseClient:
    def __init__(self, settings: DatabaseSettings) -> None:
        self._settings = settings

    def fetch_media_file(self, media_file_id: uuid.UUID) -> MediaFileRecord | None:
        with psycopg.connect(self._settings.dsn, row_factory=dict_row) as connection:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    select
                        mf.media_file_id,
                        mf.kind,
                        mf.bucket,
                        mf.object_key,
                        mf.original_filename,
                        mf.content_type,
                        mf.processing_status,
                        mm.gps_lat,
                        mm.gps_lon,
                        mm.camera_make,
                        mm.camera_model,
                        mm.size_bytes,
                        mm.checksum_sha256,
                        mm.captured_at,
                        mm.uploaded_at,
                        mm.width,
                        mm.height,
                        mm.duration_ms,
                        mm.metadata_json
                    from media_files mf
                    join media_metadata mm on mm.media_file_id = mf.media_file_id
                    where mf.media_file_id = %s
                    """,
                    (media_file_id,),
                )
                row = cursor.fetchone()

        if row is None:
            return None

        metadata = {
            "gps_lat": row["gps_lat"],
            "gps_lon": row["gps_lon"],
            "camera_make": row["camera_make"],
            "camera_model": row["camera_model"],
            "size_bytes": row["size_bytes"],
            "checksum_sha256": row["checksum_sha256"],
            "captured_at": row["captured_at"],
            "uploaded_at": row["uploaded_at"],
            "width": row["width"],
            "height": row["height"],
            "duration_ms": row["duration_ms"],
            "metadata_json": row["metadata_json"] or {},
        }

        return MediaFileRecord(
            media_file_id=row["media_file_id"],
            kind=row["kind"],
            bucket=row["bucket"],
            object_key=row["object_key"],
            original_filename=row["original_filename"],
            content_type=row["content_type"],
            processing_status=row["processing_status"],
            metadata=metadata,
        )

    def mark_processing(self, media_file_id: uuid.UUID) -> None:
        self._update_status(media_file_id, "PROCESSING")

    def mark_ready(self, media_file_id: uuid.UUID) -> None:
        self._update_status(media_file_id, "READY")

    def mark_failed(self, media_file_id: uuid.UUID) -> None:
        self._update_status(media_file_id, "FAILED")

    def update_metadata(self, media_file_id: uuid.UUID, current: dict[str, Any], extracted: ExtractedMetadata) -> None:
        merged_metadata_json = dict(current.get("metadata_json") or {})
        if extracted.metadata_json:
            merged_metadata_json.update(extracted.metadata_json)
        merged_metadata_json["workerProcessedAt"] = datetime.now(timezone.utc).isoformat()
        merged_metadata_json = sanitize_json_value(merged_metadata_json)

        payload = {
            "gps_lat": current.get("gps_lat") if current.get("gps_lat") is not None else extracted.gps_lat,
            "gps_lon": current.get("gps_lon") if current.get("gps_lon") is not None else extracted.gps_lon,
            "camera_make": sanitize_text(current.get("camera_make") or extracted.camera_make),
            "camera_model": sanitize_text(current.get("camera_model") or extracted.camera_model),
            "size_bytes": current.get("size_bytes"),
            "checksum_sha256": current.get("checksum_sha256"),
            "captured_at": current.get("captured_at") or extracted.captured_at,
            "uploaded_at": current.get("uploaded_at"),
            "width": current.get("width") or extracted.width,
            "height": current.get("height") or extracted.height,
            "duration_ms": current.get("duration_ms"),
            "metadata_json": Json(merged_metadata_json),
        }

        with psycopg.connect(self._settings.dsn) as connection:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    update media_metadata
                    set
                        gps_lat = %s,
                        gps_lon = %s,
                        camera_make = %s,
                        camera_model = %s,
                        size_bytes = %s,
                        checksum_sha256 = %s,
                        captured_at = %s,
                        uploaded_at = %s,
                        width = %s,
                        height = %s,
                        duration_ms = %s,
                        metadata_json = %s,
                        updated_at = now()
                    where media_file_id = %s
                    """,
                    (
                        payload["gps_lat"],
                        payload["gps_lon"],
                        payload["camera_make"],
                        payload["camera_model"],
                        payload["size_bytes"],
                        payload["checksum_sha256"],
                        payload["captured_at"],
                        payload["uploaded_at"],
                        payload["width"],
                        payload["height"],
                        payload["duration_ms"],
                        payload["metadata_json"],
                        media_file_id,
                    ),
                )
            connection.commit()

    def upsert_thumbnail(self, media_file_id: uuid.UUID, thumbnail: ThumbnailResult) -> None:
        with psycopg.connect(self._settings.dsn) as connection:
            with connection.cursor() as cursor:
                cursor.execute(
                    """
                    insert into media_derivatives (
                        media_file_id,
                        kind,
                        bucket,
                        object_key,
                        content_type,
                        width,
                        height,
                        size_bytes,
                        created_at
                    )
                    values (%s, 'THUMBNAIL', %s, %s, 'image/jpeg', %s, %s, %s, now())
                    on conflict (media_file_id, kind)
                    do update set
                        bucket = excluded.bucket,
                        object_key = excluded.object_key,
                        content_type = excluded.content_type,
                        width = excluded.width,
                        height = excluded.height,
                        size_bytes = excluded.size_bytes
                    """,
                    (
                        media_file_id,
                        thumbnail.bucket,
                        thumbnail.object_key,
                        thumbnail.width,
                        thumbnail.height,
                        thumbnail.size_bytes,
                    ),
                )
            connection.commit()

    def _update_status(self, media_file_id: uuid.UUID, status: str) -> None:
        with psycopg.connect(self._settings.dsn) as connection:
            with connection.cursor() as cursor:
                cursor.execute(
                    "update media_files set processing_status = %s where media_file_id = %s",
                    (status, media_file_id),
                )
            connection.commit()


class ObjectStorageClient:
    def __init__(self, settings: ObjectStorageSettings) -> None:
        self._settings = settings
        self._client = boto3.client(
            "s3",
            endpoint_url=settings.endpoint,
            aws_access_key_id=settings.access_key,
            aws_secret_access_key=settings.secret_key,
            region_name=settings.region,
            config=Config(s3={"addressing_style": "path" if settings.path_style else "virtual"}),
        )

    def download(self, bucket: str, object_key: str) -> bytes:
        response = self._client.get_object(Bucket=bucket, Key=object_key)
        return response["Body"].read()

    def upload_thumbnail(self, media_file_id: uuid.UUID, image_bytes: bytes) -> ThumbnailResult:
        with Image.open(io.BytesIO(image_bytes)) as image:
            width, height = image.size

        object_key = f"derivatives/{media_file_id}/thumbnail.jpg"
        self._client.put_object(
            Bucket=self._settings.thumbnail_bucket,
            Key=object_key,
            Body=image_bytes,
            ContentType="image/jpeg",
        )

        return ThumbnailResult(
            bucket=self._settings.thumbnail_bucket,
            object_key=object_key,
            width=width,
            height=height,
            size_bytes=len(image_bytes),
        )


class ImageProcessor:
    THUMBNAIL_SIZE = (512, 512)

    def extract_metadata(self, source: bytes) -> ExtractedMetadata:
        with Image.open(io.BytesIO(source)) as image:
            exif = image.getexif()
            metadata_json: dict[str, Any] = {
                "imageMode": image.mode,
                "imageFormat": image.format,
            }
            if exif:
                metadata_json["exifKeys"] = list(exif.keys())

            gps_info = self._gps_info(exif)
            return ExtractedMetadata(
                width=image.width,
                height=image.height,
                captured_at=self._captured_at(exif),
                camera_make=self._string_tag(exif, "Make"),
                camera_model=self._string_tag(exif, "Model"),
                gps_lat=gps_info[0],
                gps_lon=gps_info[1],
                metadata_json=metadata_json,
            )

    def create_thumbnail(self, source: bytes) -> bytes:
        with Image.open(io.BytesIO(source)) as image:
            image = ImageOps.exif_transpose(image)
            image = image.convert("RGB")
            image.thumbnail(self.THUMBNAIL_SIZE)

            output = io.BytesIO()
            image.save(output, format="JPEG", quality=85, optimize=True)
            return output.getvalue()

    def _captured_at(self, exif: Image.Exif) -> datetime | None:
        for tag_name in ("DateTimeOriginal", "DateTimeDigitized", "DateTime"):
            raw_value = self._string_tag(exif, tag_name)
            if not raw_value:
                continue
            try:
                return datetime.strptime(raw_value, "%Y:%m:%d %H:%M:%S").replace(tzinfo=timezone.utc)
            except ValueError:
                LOGGER.debug("Could not parse EXIF datetime %s", raw_value)
        return None

    def _string_tag(self, exif: Image.Exif, tag_name: str) -> str | None:
        tag_id = next((tag_id for tag_id, name in ExifTags.TAGS.items() if name == tag_name), None)
        if tag_id is None:
            return None

        value = exif.get(tag_id)
        if value is None:
            return None

        return sanitize_text(str(value))

    def _gps_info(self, exif: Image.Exif) -> tuple[float | None, float | None]:
        gps_tag_id = next((tag_id for tag_id, name in ExifTags.TAGS.items() if name == "GPSInfo"), None)
        if gps_tag_id is None:
            return None, None

        gps_raw = self._gps_ifd(exif, gps_tag_id)
        if not isinstance(gps_raw, dict) or not gps_raw:
            return None, None

        gps_data = {
            ExifTags.GPSTAGS.get(key, key): value
            for key, value in gps_raw.items()
        }
        lat = self._gps_coordinate(gps_data.get("GPSLatitude"), gps_data.get("GPSLatitudeRef"))
        lon = self._gps_coordinate(gps_data.get("GPSLongitude"), gps_data.get("GPSLongitudeRef"))
        return lat, lon

    def _gps_ifd(self, exif: Image.Exif, gps_tag_id: int) -> dict[int, Any] | None:
        try:
            if hasattr(exif, "get_ifd"):
                gps_ifd = exif.get_ifd(gps_tag_id)
                if isinstance(gps_ifd, dict):
                    return gps_ifd
        except Exception:
            LOGGER.debug("Could not read GPS IFD via get_ifd", exc_info=True)

        gps_raw = exif.get(gps_tag_id)
        if isinstance(gps_raw, dict):
            return gps_raw

        return None

    def _gps_coordinate(self, values: Any, reference: Any) -> float | None:
        if not values or not reference:
            return None

        degrees = self._rational_to_float(values[0])
        minutes = self._rational_to_float(values[1])
        seconds = self._rational_to_float(values[2])
        coordinate = degrees + minutes / 60 + seconds / 3600

        if str(reference).upper() in {"S", "W"}:
            coordinate *= -1

        return coordinate

    def _rational_to_float(self, value: Any) -> float:
        if hasattr(value, "numerator") and hasattr(value, "denominator"):
            return float(Decimal(value.numerator) / Decimal(value.denominator))
        if isinstance(value, tuple) and len(value) == 2 and value[1] != 0:
            return float(Decimal(value[0]) / Decimal(value[1]))
        return float(value)


class MediaProcessor:
    def __init__(
        self,
        database: DatabaseClient,
        storage: ObjectStorageClient,
        image_processor: ImageProcessor,
    ) -> None:
        self._database = database
        self._storage = storage
        self._image_processor = image_processor

    def process(self, message: MediaFileQueueMessage) -> None:
        record = self._database.fetch_media_file(message.media_file_id)
        if record is None:
            LOGGER.warning("Media file %s no longer exists", message.media_file_id)
            return

        self._database.mark_processing(record.media_file_id)

        source = self._storage.download(record.bucket, record.object_key)
        extracted_metadata = self._extract_metadata(record, source)
        self._database.update_metadata(record.media_file_id, record.metadata, extracted_metadata)

        if record.kind == "IMAGE":
            thumbnail_bytes = self._image_processor.create_thumbnail(source)
            thumbnail = self._storage.upload_thumbnail(record.media_file_id, thumbnail_bytes)
            self._database.upsert_thumbnail(record.media_file_id, thumbnail)

        self._database.mark_ready(record.media_file_id)

    def fail(self, media_file_id: uuid.UUID) -> None:
        self._database.mark_failed(media_file_id)

    def _extract_metadata(self, record: MediaFileRecord, source: bytes) -> ExtractedMetadata:
        if record.kind == "IMAGE":
            return self._image_processor.extract_metadata(source)

        return ExtractedMetadata(
            metadata_json={
                "workerProcessedAt": datetime.now(timezone.utc).isoformat(),
                "workerNote": "No specialized processor implemented for this media kind yet.",
            }
        )


class QueueConsumer:
    def __init__(self, settings: RabbitMqSettings, processor: MediaProcessor) -> None:
        self._settings = settings
        self._processor = processor

    def run(self) -> None:
        credentials = pika.PlainCredentials(
            self._settings.username,
            self._settings.password,
        )
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(
                host=self._settings.host,
                port=self._settings.port,
                virtual_host=self._settings.virtual_host,
                credentials=credentials,
            )
        )
        channel = connection.channel()
        channel.queue_declare(queue=self._settings.queue_name, durable=True)
        channel.basic_qos(prefetch_count=1)

        LOGGER.info("Listening on queue '%s'", self._settings.queue_name)

        def on_message(ch: pika.adapters.blocking_connection.BlockingChannel, method: Any, _: Any, body: bytes) -> None:
            media_file_id: uuid.UUID | None = None
            try:
                message = MediaFileQueueMessage.from_body(body)
                media_file_id = message.media_file_id
                LOGGER.info("Processing media file %s", media_file_id)
                self._processor.process(message)
                ch.basic_ack(delivery_tag=method.delivery_tag)
                LOGGER.info("Finished media file %s", media_file_id)
            except Exception:
                LOGGER.exception("Failed processing message")
                if media_file_id is not None:
                    try:
                        self._processor.fail(media_file_id)
                    except Exception:
                        LOGGER.exception("Failed marking media file %s as FAILED", media_file_id)
                ch.basic_ack(delivery_tag=method.delivery_tag)

        channel.basic_consume(
            queue=self._settings.queue_name,
            on_message_callback=on_message,
            auto_ack=False,
        )

        try:
            channel.start_consuming()
        except KeyboardInterrupt:
            LOGGER.info("Stopping worker")
        finally:
            if connection.is_open:
                connection.close()


def parse_datetime(value: str | None) -> datetime | None:
    if not value:
        return None

    normalized = value.replace("Z", "+00:00")
    return datetime.fromisoformat(normalized)


def sanitize_text(value: str | None) -> str | None:
    if value is None:
        return None

    cleaned = value.replace("\x00", "").strip()
    return cleaned or None


def sanitize_json_value(value: Any) -> Any:
    if isinstance(value, str):
        return value.replace("\x00", "")
    if isinstance(value, dict):
        return {sanitize_json_value(key): sanitize_json_value(item) for key, item in value.items()}
    if isinstance(value, list):
        return [sanitize_json_value(item) for item in value]
    return value


def configure_logging() -> None:
    logging.basicConfig(
        level=os.getenv("WORKER_LOG_LEVEL", "INFO").upper(),
        format="%(asctime)s %(levelname)s %(name)s %(message)s",
    )


def main() -> int:
    configure_logging()
    settings = WorkerSettings.from_env()

    processor = MediaProcessor(
        database=DatabaseClient(settings.database),
        storage=ObjectStorageClient(settings.object_storage),
        image_processor=ImageProcessor(),
    )
    consumer = QueueConsumer(settings.rabbitmq, processor)
    consumer.run()
    return 0


if __name__ == "__main__":
    sys.exit(main())
