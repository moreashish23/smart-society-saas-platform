CREATE TABLE IF NOT EXISTS vendors (
    id               UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    society_id       UUID           NOT NULL,
    user_id          UUID           NOT NULL,
    business_name    VARCHAR(255)   NOT NULL,
    contact_person   VARCHAR(100)   NOT NULL,
    contact_email    VARCHAR(255)   NOT NULL,
    contact_phone    VARCHAR(20)    NOT NULL,
    service_category VARCHAR(30)    NOT NULL
    CHECK (service_category IN (
           'PLUMBING','ELECTRICITY','LIFT_MAINTENANCE','HOUSEKEEPING',
           'SECURITY','CARPENTRY','PAINTING','PEST_CONTROL',
           'INTERNET','GENERAL_MAINTENANCE','OTHER'
                               )),
    description      TEXT,
    address          VARCHAR(500),
    rating           NUMERIC(3,2)   NOT NULL DEFAULT 0.00
    CHECK (rating BETWEEN 0.00 AND 5.00),
    total_jobs       INT            NOT NULL DEFAULT 0,
    completed_jobs   INT            NOT NULL DEFAULT 0,
    status           VARCHAR(30)    NOT NULL DEFAULT 'PENDING_APPROVAL'
    CHECK (status IN (
           'ACTIVE','INACTIVE','SUSPENDED','PENDING_APPROVAL'
                     )),
    approved_by      UUID,
    approved_at      TIMESTAMP,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_vendor_user_society UNIQUE (user_id, society_id),
    CONSTRAINT chk_vendor_jobs CHECK (completed_jobs <= total_jobs)
    );

CREATE TABLE IF NOT EXISTS vendor_jobs (
    id              UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    vendor_id       UUID        NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
    society_id      UUID        NOT NULL,
    complaint_id    UUID        NOT NULL,
    complaint_title VARCHAR(255),
    status          VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED'
    CHECK (status IN (
           'ASSIGNED','ACCEPTED','IN_PROGRESS','COMPLETED','CANCELLED'
                     )),
    assigned_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    accepted_at     TIMESTAMP,
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    cancelled_at    TIMESTAMP,
    notes           TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_vendor_complaint UNIQUE (vendor_id, complaint_id)
    );


CREATE TABLE IF NOT EXISTS vendor_ratings (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    vendor_id    UUID         NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
    society_id   UUID         NOT NULL,
    complaint_id UUID         NOT NULL,
    rated_by     UUID         NOT NULL,
    rating       NUMERIC(3,2) NOT NULL CHECK (rating BETWEEN 1.0 AND 5.0),
    review       TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_vendor_rating_complaint UNIQUE (vendor_id, complaint_id)
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_vendors_society_id       ON vendors(society_id);
CREATE INDEX IF NOT EXISTS idx_vendors_user_id          ON vendors(user_id);
CREATE INDEX IF NOT EXISTS idx_vendors_status           ON vendors(society_id, status);
CREATE INDEX IF NOT EXISTS idx_vendors_category         ON vendors(society_id, service_category);
CREATE INDEX IF NOT EXISTS idx_vendors_rating           ON vendors(society_id, rating DESC);
CREATE INDEX IF NOT EXISTS idx_vendor_jobs_vendor_id    ON vendor_jobs(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_jobs_society_id   ON vendor_jobs(society_id);
CREATE INDEX IF NOT EXISTS idx_vendor_jobs_complaint_id ON vendor_jobs(complaint_id);
CREATE INDEX IF NOT EXISTS idx_vendor_jobs_status       ON vendor_jobs(vendor_id, status);
CREATE INDEX IF NOT EXISTS idx_vendor_ratings_vendor_id ON vendor_ratings(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_ratings_society   ON vendor_ratings(society_id);


CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_vendors_updated_at
    BEFORE UPDATE ON vendors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_vendor_jobs_updated_at
    BEFORE UPDATE ON vendor_jobs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();