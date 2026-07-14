package com.smartsociety.analytics.exception;

public class AnalyticsServiceException extends RuntimeException {

    private final String errorCode;

    public AnalyticsServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}