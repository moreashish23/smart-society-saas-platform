CREATE TABLE IF NOT EXISTS audit_logs (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    society_id  UUID,
    user_id     UUID,
    action      VARCHAR(50)  NOT NULL
    CHECK (action IN (
           'LOGIN','LOGOUT','LOGOUT_ALL','REGISTER',
           'PASSWORD_CHANGE','PASSWORD_RESET','FAILED_LOGIN',
           'SOCIETY_CREATE','SOCIETY_UPDATE','SOCIETY_ACTIVATE','SOCIETY_DEACTIVATE','SOCIETY_DELETE',
           'MEMBER_ADD','MEMBER_REMOVE','MEMBER_ROLE_UPDATE',
           'COMPLAINT_CREATE','COMPLAINT_ASSIGN','COMPLAINT_ACCEPT',
           'COMPLAINT_WORK_START','COMPLAINT_WORK_COMPLETE',
           'COMPLAINT_RESOLVE','COMPLAINT_CLOSE','COMPLAINT_REOPEN',
           'COMPLAINT_CANCEL','COMPLAINT_ESCALATE',
           'VENDOR_REGISTER','VENDOR_APPROVE','VENDOR_UPDATE',
           'VENDOR_SUSPEND','VENDOR_ACTIVATE','VENDOR_RATED',
           'NOTICE_CREATE','NOTICE_UPDATE','NOTICE_PUBLISH',
           'NOTICE_ARCHIVE','NOTICE_DELETE',
           'DATA_EXPORT','SETTINGS_CHANGE'
                     )),
    entity_type VARCHAR(50),
    entity_id   UUID,
    description TEXT,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_audit_society_date  ON audit_logs(society_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_user_date     ON audit_logs(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_action        ON audit_logs(action, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_entity        ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at    ON audit_logs(created_at DESC);

CREATE RULE no_update_audit AS ON UPDATE TO audit_logs DO INSTEAD NOTHING;
CREATE RULE no_delete_audit AS ON DELETE TO audit_logs DO INSTEAD NOTHING;