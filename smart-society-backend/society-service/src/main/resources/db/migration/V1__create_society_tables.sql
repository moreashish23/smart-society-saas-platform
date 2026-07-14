CREATE TABLE IF NOT EXISTS societies (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    code                VARCHAR(50)  NOT NULL UNIQUE,
    description         TEXT,
    address_line1       VARCHAR(255) NOT NULL,
    address_line2       VARCHAR(255),
    city                VARCHAR(100) NOT NULL,
    state               VARCHAR(100) NOT NULL,
    pincode             VARCHAR(20)  NOT NULL,
    country             VARCHAR(100) NOT NULL DEFAULT 'India',
    contact_email       VARCHAR(255) NOT NULL,
    contact_phone       VARCHAR(20)  NOT NULL,
    total_units         INT          NOT NULL DEFAULT 0,
    total_floors        INT          NOT NULL DEFAULT 0,
    status              VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED')),
    subscription_plan   VARCHAR(30)  NOT NULL DEFAULT 'BASIC'
    CHECK (subscription_plan IN ('BASIC','PROFESSIONAL','ENTERPRISE')),
    subscription_expiry TIMESTAMP,
    logo_url            VARCHAR(500),
    created_by          UUID         NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS society_members (
    id          UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    society_id  UUID        NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL,
    role        VARCHAR(30) NOT NULL
    CHECK (role IN (
           'SUPER_ADMIN','SOCIETY_MANAGER','COMMITTEE_MEMBER',
           'RESIDENT','MAINTENANCE_STAFF','VENDOR'
                   )),
    flat_number VARCHAR(20),
    block       VARCHAR(20),
    floor       INT,
    status      VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE','INACTIVE','PENDING')),
    joined_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    left_at     TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (society_id, user_id)
    );

CREATE TABLE IF NOT EXISTS notices (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    society_id   UUID         NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    notice_type  VARCHAR(30)  NOT NULL DEFAULT 'GENERAL'
    CHECK (notice_type IN (
           'WATER_SHUTDOWN','MAINTENANCE','EMERGENCY_ALERT',
           'SOCIETY_MEETING','GENERAL'
                          )),
    status       VARCHAR(30)  NOT NULL DEFAULT 'DRAFT'
    CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    priority     BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP,
    expires_at   TIMESTAMP,
    created_by   UUID         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_societies_code          ON societies(code);
CREATE INDEX IF NOT EXISTS idx_societies_status        ON societies(status);
CREATE INDEX IF NOT EXISTS idx_society_members_society ON society_members(society_id);
CREATE INDEX IF NOT EXISTS idx_society_members_user    ON society_members(user_id);
CREATE INDEX IF NOT EXISTS idx_society_members_status  ON society_members(society_id, status);
CREATE INDEX IF NOT EXISTS idx_notices_society         ON notices(society_id);
CREATE INDEX IF NOT EXISTS idx_notices_status          ON notices(society_id, status);
CREATE INDEX IF NOT EXISTS idx_notices_type            ON notices(notice_type);
CREATE INDEX IF NOT EXISTS idx_notices_published       ON notices(published_at DESC);