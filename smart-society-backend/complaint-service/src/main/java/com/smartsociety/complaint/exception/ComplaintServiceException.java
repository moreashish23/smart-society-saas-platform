package com.smartsociety.complaint.exception;

public class ComplaintServiceException extends RuntimeException {

    private final String errorCode;

    public ComplaintServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}