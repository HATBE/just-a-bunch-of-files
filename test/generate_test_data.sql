CREATE EXTENSION IF NOT EXISTS pgcrypto;

WITH owner AS (
    SELECT user_id
    FROM users
    ORDER BY created_at
    LIMIT 1
),

generated_media AS (
    SELECT
        gen_random_uuid() AS media_file_id,
        gs AS n,
        CASE
            WHEN gs % 5 = 0 THEN 'VIDEO'::MediaFileKind
            ELSE 'IMAGE'::MediaFileKind
        END AS kind
    FROM generate_series(1, 50) AS gs
),

inserted_media AS (
    INSERT INTO media_file (
        created_at,
        deleted_at,
        updated_at,
        version,
        media_file_id,
        owner_user_id,
        storage_bucket_object_key,
        storage_bucket,
        kind,
        processing_state
    )
    SELECT
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        0,
        gm.media_file_id,
        o.user_id,
        'test/media/' || gm.media_file_id ||
            CASE
                WHEN gm.kind = 'VIDEO'::MediaFileKind THEN '.mp4'
                ELSE '.jpg'
            END,
        'test-media',
        gm.kind,
        'READY'::MediaFileProcessingState
    FROM generated_media gm
    CROSS JOIN owner o
    RETURNING
        media_file_id,
        kind,
        storage_bucket_object_key
),

inserted_albums AS (
    INSERT INTO album (
        created_at,
        deleted_at,
        updated_at,
        version,
        album_id,
        owner_user_id,
        name
    )
    SELECT
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        0,
        gen_random_uuid(),
        o.user_id,
        album_name
    FROM owner o
    CROSS JOIN (
        VALUES
            ('Favorites'),
            ('Zurich'),
            ('Videos'),
            ('Archive')
    ) AS albums(album_name)
    RETURNING
        album_id,
        name
),

inserted_album_media_files AS (
    INSERT INTO album_media_files (
        album_id,
        media_file_id
    )
    SELECT
        ia.album_id,
        im.media_file_id
    FROM inserted_albums ia
    JOIN inserted_media im
        ON (
            ia.name = 'Favorites'
            AND im.media_file_id IN (
                SELECT media_file_id
                FROM inserted_media
                ORDER BY media_file_id
                LIMIT 12
            )
        )
        OR (
            ia.name = 'Zurich'
            AND im.media_file_id IN (
                SELECT media_file_id
                FROM inserted_media
                ORDER BY media_file_id
                OFFSET 12
                LIMIT 15
            )
        )
        OR (
            ia.name = 'Videos'
            AND im.kind = 'VIDEO'::MediaFileKind
        )
        OR (
            ia.name = 'Archive'
            AND im.media_file_id IN (
                SELECT media_file_id
                FROM inserted_media
                ORDER BY media_file_id
                OFFSET 27
                LIMIT 10
            )
        )
    RETURNING
        album_id,
        media_file_id
),

inserted_metadata AS (
    INSERT INTO media_file_metadata (
        gps_lat,
        gps_lon,
        height,
        width,
        captured_at,
        created_at,
        deleted_at,
        duration_ms,
        size_bytes,
        updated_at,
        uploaded_at,
        version,
        media_file_id,
        checksum_sha256,
        original_filename,
        camera_make,
        camera_model,
        content_type,
        metadata_json
    )
    SELECT
        47.3769 + ((random() - 0.5) / 10),
        8.5417 + ((random() - 0.5) / 10),
        CASE
            WHEN im.kind = 'VIDEO'::MediaFileKind THEN 1080
            ELSE 3000
        END,
        CASE
            WHEN im.kind = 'VIDEO'::MediaFileKind THEN 1920
            ELSE 4000
        END,
        CURRENT_TIMESTAMP - (random() * INTERVAL '365 days'),
        CURRENT_TIMESTAMP,
        NULL,
        CASE
            WHEN im.kind = 'VIDEO'::MediaFileKind THEN floor(10000 + random() * 300000)::bigint
            ELSE NULL
        END,
        floor(100000 + random() * 9000000)::bigint,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0,
        im.media_file_id,
        md5(random()::text) || md5(random()::text),
        CASE
            WHEN im.kind = 'VIDEO'::MediaFileKind THEN im.media_file_id || '.mp4'
            ELSE im.media_file_id || '.jpg'
        END,
        'TestCam',
        'Model-' || floor(random() * 10)::int,
        CASE
            WHEN im.kind = 'VIDEO'::MediaFileKind THEN 'video/mp4'
            ELSE 'image/jpeg'
        END,
        jsonb_build_object(
            'source', 'test-seed',
            'generated', true,
            'kind', im.kind::varchar
        )
    FROM inserted_media im
    RETURNING media_file_id
)

INSERT INTO media_file_thumbnail (
    height,
    width,
    created_at,
    deleted_at,
    updated_at,
    version,
    media_file_id,
    storage_bucket_object_key,
    content_type,
    storage_bucket
)
SELECT
    180,
    320,
    CURRENT_TIMESTAMP,
    NULL,
    CURRENT_TIMESTAMP,
    0,
    im.media_file_id,
    'test/thumbnails/' || im.media_file_id || '.jpg',
    'image/jpeg',
    'test-media'
FROM inserted_media im;