-- Initlal admin user, also created in REALM-IMPORT for keycloak
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