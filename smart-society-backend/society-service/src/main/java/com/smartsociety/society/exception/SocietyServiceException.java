package com.smartsociety.society.exception;

public class SocietyServiceException extends RuntimeException {

    private final String errorCode;

    public SocietyServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}