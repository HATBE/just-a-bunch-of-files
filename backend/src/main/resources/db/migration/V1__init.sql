create extension if not exists pgcrypto;

create type media_kind as enum ('IMAGE', 'VIDEO');

create type media_processing_status as enum (
  'UPLOADED',
  'QUEUED',
  'PROCESSING',
  'READY',
  'FAILED'
);

create type media_derivative_kind as enum (
  'THUMBNAIL'
);

create table if not exists users (
    user_id uuid primary key default gen_random_uuid(),
    username varchar(255) not null unique,
    created_at timestamptz not null default now()
);

create table if not exists albums (
    album_id uuid primary key default gen_random_uuid(),
    owner_user_id uuid not null references users(user_id) on delete cascade,
    name varchar(255) not null,
    created_at timestamptz not null default now()
);

create table if not exists media_files (
    media_file_id uuid primary key default gen_random_uuid(),
    owner_user_id uuid not null references users(user_id) on delete cascade,
    kind media_kind not null,
    processing_status media_processing_status not null default 'UPLOADED',
    bucket varchar(255) not null,
    object_key varchar(1024) not null unique,
    original_filename varchar(512) not null,
    content_type varchar(255) not null,
    size_bytes bigint,
    checksum_sha256 char(64),
    captured_at timestamptz,
    uploaded_at timestamptz not null default now(),
    width int,
    height int,
    duration_ms bigint,
    created_at timestamptz not null default now(),

    constraint chk_media_files_size_bytes_nonnegative check (size_bytes is null or size_bytes >= 0),
    constraint chk_media_files_width_positive check (width is null or width > 0),
    constraint chk_media_files_height_positive check (height is null or height > 0),
    constraint chk_media_files_duration_nonnegative check (duration_ms is null or duration_ms >= 0)
);

create table if not exists album_media_files (
    album_id uuid not null references albums(album_id) on delete cascade,
    media_file_id uuid not null references media_files(media_file_id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (album_id, media_file_id)
);

create table if not exists media_derivatives (
    derivative_id uuid primary key default gen_random_uuid(),
    media_file_id uuid not null references media_files(media_file_id) on delete cascade,
    kind media_derivative_kind not null,
    bucket varchar(255) not null,
    object_key varchar(1024) not null unique,
    content_type varchar(255) not null default 'image/jpeg',
    width int not null,
    height int not null,
    size_bytes bigint,
    created_at timestamptz not null default now(),

    constraint chk_media_derivatives_width_positive check (width > 0),
    constraint chk_media_derivatives_height_positive check (height > 0),
    constraint chk_media_derivatives_size_bytes_nonnegative check (size_bytes is null or size_bytes >= 0),
    constraint uq_media_derivatives_variant unique (media_file_id, kind, width, height)
);

create table if not exists media_metadata (
    media_file_id uuid primary key references media_files(media_file_id) on delete cascade,
    gps_lat double precision,
    gps_lon double precision,
    camera_make text,
    camera_model text,
    metadata_json jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),

    constraint chk_media_metadata_gps_lat check (gps_lat is null or (gps_lat >= -90 and gps_lat <= 90)),
    constraint chk_media_metadata_gps_lon check (gps_lon is null or (gps_lon >= -180 and gps_lon <= 180)),
    constraint chk_media_metadata_gps_pair check ((gps_lat is null and gps_lon is null) or (gps_lat is not null and gps_lon is not null))
);

create index if not exists idx_albums_owner_user_id on albums(owner_user_id);
create index if not exists idx_media_files_owner_user_captured_uploaded on media_files(owner_user_id, captured_at desc, uploaded_at desc, media_file_id asc);
create index if not exists idx_media_derivatives_file_kind on media_derivatives(media_file_id, kind);
create index if not exists idx_album_media_files_file_id on album_media_files(media_file_id);
create index if not exists idx_media_metadata_gps_lat_lon on media_metadata(gps_lat, gps_lon);
