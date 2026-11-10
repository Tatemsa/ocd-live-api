USE `ocd`;

-- 1. Insérer un compte Administrateur (rôle ROLE_ADMIN)
-- Mot de passe : admin123
INSERT INTO `users` (
    `email`,
    `password`,
    `first_name`,
    `last_name`,
    `role`,
    `enabled`,
    `created_at`,
    `updated_at`
) VALUES (
    'admin@ocdlive.com',
    '$2a$10$5EQheqqwJcf2K7/QRNLU3Oxc3SyhiTwyRUObaAnH.6yyW.vIHSFqC',
    'Super',
    'Admin',
    'ROLE_ADMIN',
    1,
    NOW(),
    NOW()
);

-- 2. (Optionnel) Insérer un utilisateur standard (rôle ROLE_USER)
-- Mot de passe : admin123
INSERT INTO `users` (
    `email`,
    `password`,
    `first_name`,
    `last_name`,
    `role`,
    `enabled`,
    `created_at`,
    `updated_at`
) VALUES (
    'user@ocdlive.com',
    '$2a$10$5EQheqqwJcf2K7/QRNLU3Oxc3SyhiTwyRUObaAnH.6yyW.vIHSFqC',
    'Jean',
    'Dupont',
    'ROLE_USER',
    1,
    NOW(),
    NOW()
);