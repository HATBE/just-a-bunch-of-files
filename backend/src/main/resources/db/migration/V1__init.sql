create extension if not exists pgcrypto;

create table if not exists users (
    user_id uuid primary key default gen_random_uuid(),
    username varchar(255) not null
);

create table if not exists albums (
    album_id uuid primary key default gen_random_uuid(),
    owner_user_id uuid not null references users(user_id) on delete cascade,
    name varchar(255) not null,
    created_at timestamptz not null default now()
);

create table if not exists media_files (
    file_id uuid primary key default gen_random_uuid(),
    owner_user_id uuid not null references users(user_id) on delete cascade,
    kind varchar(32) not null,
    bucket varchar(255) not null,
    object_key varchar(512) not null unique,
    original_filename varchar(512) not null,
    content_type varchar(255) not null,
    size_bytes bigint not null,
    uploaded_at timestamptz not null default now(),
    constraint media_files_kind_check check (kind in ('IMAGE', 'VIDEO'))
);

create table if not exists album_media_files (
    album_id uuid not null references albums(album_id) on delete cascade,
    file_id uuid not null references media_files(file_id) on delete cascade,
    primary key (album_id, file_id)
);

create index if not exists idx_albums_owner_user_id on albums(owner_user_id);
create index if not exists idx_media_files_owner_user_id on media_files(owner_user_id);
create index if not exists idx_album_media_files_file_id on album_media_files(file_id);
