create extension if not exists pgcrypto;

create table if not exists users (
    user_id uuid primary key default gen_random_uuid(),
    username varchar(255) not null
);