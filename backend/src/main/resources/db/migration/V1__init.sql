
create type MediaFileKind as enum ('IMAGE','VIDEO');

create cast (varchar as MediaFileKind) with inout as implicit;

create cast (MediaFileKind as varchar) with inout as implicit;

create type MediaFileProcessingState as enum ('FAILED','PROCESSING','QUEUED','READY','UPLOADED');

create cast (varchar as MediaFileProcessingState) with inout as implicit;

create cast (MediaFileProcessingState as varchar) with inout as implicit;

create table media_file (
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
owner_user_id uuid not null,
storage_bucket_object_key varchar(1024) not null unique,
storage_bucket varchar(255) not null,
kind MediaFileKind not null,
processing_state MediaFileProcessingState not null,
primary key (media_file_id)
);

create table media_file_metadata (
gps_lat float(53),
gps_lon float(53),
height integer,
width integer,
captured_at timestamp(6) with time zone,
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
duration_ms bigint,
size_bytes bigint,
updated_at timestamp(6) with time zone not null,
uploaded_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
checksum_sha256 varchar(64),
original_filename varchar(512) not null,
camera_make varchar(255),
camera_model varchar(255),
content_type varchar(255) not null,
metadata_json jsonb not null,
primary key (media_file_id)
);

create table media_file_thumbnail (
height integer,
width integer,
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
storage_bucket_object_key varchar(1024) not null unique,
content_type varchar(255) not null,
storage_bucket varchar(255) not null,
primary key (media_file_id)
);

create table users (
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
keycloak_user_id uuid not null unique,
user_id uuid not null,
username varchar(80) not null unique,
email varchar(320) unique,
primary key (user_id)
);

alter table if exists media_file
add constraint FKhpr2vsm6l6xlj286h79rchr2j
foreign key (owner_user_id)
references users;

alter table if exists media_file_metadata
add constraint FKp7s3an6047bobsasajtvflfh5
foreign key (media_file_id)
references media_file;

alter table if exists media_file_thumbnail
add constraint FKp4upmqijohaa1v8y75mfpym55
foreign key (media_file_id)
references media_file;

create type MediaFileKind as enum ('IMAGE','VIDEO');

create cast (varchar as MediaFileKind) with inout as implicit;

create cast (MediaFileKind as varchar) with inout as implicit;

create type MediaFileProcessingState as enum ('FAILED','PROCESSING','QUEUED','READY','UPLOADED');

create cast (varchar as MediaFileProcessingState) with inout as implicit;

create cast (MediaFileProcessingState as varchar) with inout as implicit;

create table media_file (
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
owner_user_id uuid not null,
storage_bucket_object_key varchar(1024) not null unique,
storage_bucket varchar(255) not null,
kind MediaFileKind not null,
processing_state MediaFileProcessingState not null,
primary key (media_file_id)
);

create table media_file_metadata (
gps_lat float(53),
gps_lon float(53),
height integer,
width integer,
captured_at timestamp(6) with time zone,
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
duration_ms bigint,
size_bytes bigint,
updated_at timestamp(6) with time zone not null,
uploaded_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
checksum_sha256 varchar(64),
original_filename varchar(512) not null,
camera_make varchar(255),
camera_model varchar(255),
content_type varchar(255) not null,
metadata_json jsonb not null,
primary key (media_file_id)
);

create table media_file_thumbnail (
height integer,
width integer,
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
storage_bucket_object_key varchar(1024) not null unique,
content_type varchar(255) not null,
storage_bucket varchar(255) not null,
primary key (media_file_id)
);

create table users (
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
keycloak_user_id uuid not null unique,
user_id uuid not null,
username varchar(80) not null unique,
email varchar(320) unique,
primary key (user_id)
);

alter table if exists media_file
add constraint FKhpr2vsm6l6xlj286h79rchr2j
foreign key (owner_user_id)
references users;

alter table if exists media_file_metadata
add constraint FKp7s3an6047bobsasajtvflfh5
foreign key (media_file_id)
references media_file;

alter table if exists media_file_thumbnail
add constraint FKp4upmqijohaa1v8y75mfpym55
foreign key (media_file_id)
references media_file;

create type MediaFileKind as enum ('IMAGE','VIDEO');

create cast (varchar as MediaFileKind) with inout as implicit;

create cast (MediaFileKind as varchar) with inout as implicit;

create type MediaFileProcessingState as enum ('FAILED','PROCESSING','QUEUED','READY','UPLOADED');

create cast (varchar as MediaFileProcessingState) with inout as implicit;

create cast (MediaFileProcessingState as varchar) with inout as implicit;

create table album_media_files (
album_id uuid not null,
media_file_id uuid not null,
primary key (album_id, media_file_id)
);

create table media_file (
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
album_id uuid not null,
media_file_id uuid not null,
owner_user_id uuid not null,
storage_bucket_object_key varchar(1024) not null unique,
name varchar(255) not null,
storage_bucket varchar(255) not null,
kind MediaFileKind not null,
processing_state MediaFileProcessingState not null,
primary key (media_file_id)
);

create table media_file_metadata (
gps_lat float(53),
gps_lon float(53),
height integer,
width integer,
captured_at timestamp(6) with time zone,
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
duration_ms bigint,
size_bytes bigint,
updated_at timestamp(6) with time zone not null,
uploaded_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
checksum_sha256 varchar(64),
original_filename varchar(512) not null,
camera_make varchar(255),
camera_model varchar(255),
content_type varchar(255) not null,
metadata_json jsonb not null,
primary key (media_file_id)
);

create table media_file_thumbnail (
height integer,
width integer,
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
media_file_id uuid not null,
storage_bucket_object_key varchar(1024) not null unique,
content_type varchar(255) not null,
storage_bucket varchar(255) not null,
primary key (media_file_id)
);

create table users (
created_at timestamp(6) with time zone not null,
deleted_at timestamp(6) with time zone,
updated_at timestamp(6) with time zone not null,
version bigint not null,
keycloak_user_id uuid not null unique,
user_id uuid not null,
username varchar(80) not null unique,
email varchar(320) unique,
primary key (user_id)
);

alter table if exists album_media_files
    add constraint FKqrw61gqhp14jm9cno0rgxqaot
        foreign key (media_file_id)
            references media_file;

alter table if exists album_media_files
    add constraint FKb7akwyp7pgqfrvblene60raxf
        foreign key (album_id)
            references media_file;

alter table if exists media_file
    add constraint FKhpr2vsm6l6xlj286h79rchr2j
        foreign key (owner_user_id)
            references users;

alter table if exists media_file_metadata
    add constraint FKp7s3an6047bobsasajtvflfh5
        foreign key (media_file_id)
            references media_file;

alter table if exists media_file_thumbnail
    add constraint FKp4upmqijohaa1v8y75mfpym55
        foreign key (media_file_id)
            references media_file;
