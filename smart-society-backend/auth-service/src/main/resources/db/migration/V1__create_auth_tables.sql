CREATE TABLE IF NOT EXISTS users (
    id                      UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    phone                   VARCHAR(20),
    role                    VARCHAR(30)  NOT NULL DEFAULT 'RESIDENT'
    CHECK (role IN (
           'SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER',
           'RESIDENT','MAINTENANCE_STAFF','VENDOR'
                   )),
    status                  VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN (
           'ACTIVE','INACTIVE','LOCKED','PENDING_VERIFICATION'
                     )),
    society_id              UUID,
    flat_number             VARCHAR(20),
    failed_login_attempts   INT          NOT NULL DEFAULT 0,
    locked_until            TIMESTAMP,
    last_login_at           TIMESTAMP,
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    device_info  VARCHAR(255),
    ip_address   VARCHAR(45),
    expires_at   TIMESTAMP    NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
    );

-- !! CRITICAL !!
-- Column name is "token" (not "token_hash") because:
--   AuthServiceImpl builds: PasswordResetToken.builder().token(rawToken)
--   PasswordResetTokenRepository.findByToken(token)
--   The raw token is stored here (not hashed) because it is short-lived (15 min)
--   and single-use. Hashing it would require the token to be looked up,
--   which means we'd need to hash every incoming token for comparison anyway.
--   Storing raw is acceptable for a 15-minute single-use token.
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_users_email               ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_society_id          ON users(society_id);
CREATE INDEX IF NOT EXISTS idx_users_role                ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status              ON users(status);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash       ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires    ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_user_id    ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_token      ON password_reset_tokens(token);