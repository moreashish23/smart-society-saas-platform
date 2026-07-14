package com.smartsociety.audit.exception;

public class AuditServiceException extends RuntimeException {

    private final String errorCode;

    public AuditServiceException(String msg, String code) {
        super(msg);
        this.errorCode = code;
    }

    public String getErrorCode() {
        return errorCode;
    }
}