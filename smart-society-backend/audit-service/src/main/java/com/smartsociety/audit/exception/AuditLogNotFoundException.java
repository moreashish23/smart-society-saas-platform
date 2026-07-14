package com.smartsociety.audit.exception;

public class AuditLogNotFoundException extends AuditServiceException {

    public AuditLogNotFoundException(String id) {
        super(
                "Audit log not found: " + id,
                "AUDIT_LOG_NOT_FOUND"
        );
    }
}