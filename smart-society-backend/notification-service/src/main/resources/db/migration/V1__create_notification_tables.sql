CREATE TABLE IF NOT EXISTS notifications (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    society_id   UUID         NOT NULL,
    recipient_id UUID,
    type         VARCHAR(50)  NOT NULL
    CHECK (type IN (
           'COMPLAINT_CREATED','COMPLAINT_ASSIGNED','COMPLAINT_ACCEPTED',
           'COMPLAINT_WORK_STARTED','COMPLAINT_WORK_COMPLETED',
           'COMPLAINT_PENDING_VERIFICATION','COMPLAINT_RESOLVED',
           'COMPLAINT_REOPENED','COMPLAINT_ESCALATED','COMPLAINT_CANCELLED',
           'NOTICE_PUBLISHED','VENDOR_APPROVED','GENERAL'
                   )),
    title        VARCHAR(255) NOT NULL,
    message      TEXT         NOT NULL,
    entity_id    UUID,
    entity_type  VARCHAR(50),
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at      TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_notifications_recipient  ON notifications(recipient_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_society    ON notifications(society_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_type       ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_entity     ON notifications(entity_id, entity_type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);

CREATE OR REPLACE FUNCTION update_read_at()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_read = TRUE AND OLD.is_read = FALSE THEN
        NEW.read_at = NOW();
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notification_read_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_read_at();