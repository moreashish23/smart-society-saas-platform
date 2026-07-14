package com.smartsociety.audit.exception;

public class AccessDeniedException extends AuditServiceException {

    public AccessDeniedException() {
        super(
                "You do not have permission to view audit logs",
                "ACCESS_DENIED"
        );
    }
}