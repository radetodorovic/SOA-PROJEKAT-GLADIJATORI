CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO "Users" ("Username", "Email", "PasswordHash", "Role", "IsBlocked")
SELECT
    'admin',
    'admin@soa.local',
    crypt('Admin123!', gen_salt('bf')),
    'Admin',
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM "Users"
    WHERE "Username" = 'admin'
       OR "Email" = 'admin@soa.local'
);

-- Login credentials after seed:
-- username: admin
-- email: admin@soa.local
-- password: Admin123!
