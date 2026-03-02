create table if not exists storage_object (
    id uuid primary key,
    bucket varchar(255) not null,
    object_key varchar(1024) not null unique,
    original_filename varchar(1024),
    content_type varchar(255),
    size_bytes bigint,
    etag varchar(255),
    uploaded_at timestamptz not null,
    metadata_json text not null default '{}'
);

create index if not exists idx_storage_object_uploaded_at on storage_object(uploaded_at desc);
create index if not exists idx_storage_object_bucket on storage_object(bucket);