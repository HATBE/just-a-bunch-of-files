#!/usr/bin/env python3
import json

import pika

RABBITMQ_HOST = "localhost"
RABBITMQ_PORT = 5672
RABBITMQ_USERNAME = "guest"
RABBITMQ_PASSWORD = "guest"
RABBITMQ_VHOST = "/"
QUEUE_NAME = "media.processing"

# Replace with a real media_files.media_file_id value from your DB.
MESSAGE = {
    "id": "7b6fd43b-aefd-4c51-89c4-074360e53c61",
    "type": "IMAGE",
}


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
    channel.basic_publish(
        exchange="",
        routing_key=QUEUE_NAME,
        body=json.dumps(MESSAGE).encode("utf-8"),
        properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
    )
    connection.close()

    print("Published message:")
    print(json.dumps(MESSAGE, indent=2))


if __name__ == "__main__":
    main()
