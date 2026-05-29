CREATE TABLE users (
    user_id UUID NOT NULL,
    keycloak_user_id UUID NOT NULL,
    username VARCHAR(80) NOT NULL,
    email VARCHAR(320),

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_users_keycloak_user_id UNIQUE (keycloak_user_id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

INSERT INTO users (
    user_id,
    keycloak_user_id,
    username,
    email,
    created_at,
    updated_at,
    version,
    deleted_at
) VALUES (
    '7dca3d46-5d1f-4dfd-8c9c-6b3c70f756f0',
    'f5b007e5-3e2b-4bc7-a127-469533644356',
    'hatbe',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    NULL
);