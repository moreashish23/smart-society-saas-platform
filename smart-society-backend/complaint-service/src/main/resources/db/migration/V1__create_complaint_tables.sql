CREATE TABLE IF NOT EXISTS complaints (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    society_id          UUID         NOT NULL,
    resident_id         UUID         NOT NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT         NOT NULL,
    category            VARCHAR(30)  NOT NULL
    CHECK (category IN (
           'WATER_LEAKAGE','PLUMBING','ELECTRICITY','LIFT_ISSUE',
           'PARKING','SECURITY','HOUSEKEEPING','INTERNET_ISSUE',
           'NOISE_COMPLAINT','OTHER'
                       )),
    priority            VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
    CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status              VARCHAR(30)  NOT NULL DEFAULT 'OPEN'
    CHECK (status IN (
           'OPEN','ASSIGNED','IN_PROGRESS','PENDING_VERIFICATION',
           'CLOSED','CANCELLED','REOPENED'
                     )),
    assigned_to_id      UUID,
    assigned_at         TIMESTAMP,
    sla_deadline        TIMESTAMP    NOT NULL,
    escalation_level    INT          NOT NULL DEFAULT 0
    CHECK (escalation_level BETWEEN 0 AND 3),
    escalated_at        TIMESTAMP,
    reopen_count        INT          NOT NULL DEFAULT 0,
    resolution_note     TEXT,
    resolved_at         TIMESTAMP,
    closed_at           TIMESTAMP,
    location            VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS complaint_timeline (
    id           UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    complaint_id UUID        NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    action       VARCHAR(30) NOT NULL
    CHECK (action IN (
           'CREATED','ASSIGNED','ACCEPTED','WORK_STARTED',
           'WORK_COMPLETED','MARKED_RESOLVED','VERIFIED_RESOLVED',
           'REOPENED','ESCALATED','CANCELLED','COMMENTED'
                     )),
    performed_by UUID        NOT NULL,
    note         TEXT,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS complaint_comments (
    id           UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    complaint_id UUID        NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    author_id    UUID        NOT NULL,
    author_role  VARCHAR(30),
    content      TEXT        NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS complaint_attachments (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    complaint_id UUID         NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    file_url     VARCHAR(500) NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    file_type    VARCHAR(100),
    file_size    BIGINT,
    uploaded_by  UUID         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_complaints_society_id  ON complaints(society_id);
CREATE INDEX IF NOT EXISTS idx_complaints_resident_id ON complaints(resident_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status      ON complaints(society_id, status);
CREATE INDEX IF NOT EXISTS idx_complaints_priority    ON complaints(society_id, priority);
CREATE INDEX IF NOT EXISTS idx_complaints_assigned_to ON complaints(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_complaints_sla         ON complaints(sla_deadline);
CREATE INDEX IF NOT EXISTS idx_complaints_escalation  ON complaints(escalation_level, status);
CREATE INDEX IF NOT EXISTS idx_complaints_created_at  ON complaints(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_timeline_complaint_id  ON complaint_timeline(complaint_id);
CREATE INDEX IF NOT EXISTS idx_comments_complaint_id  ON complaint_comments(complaint_id);