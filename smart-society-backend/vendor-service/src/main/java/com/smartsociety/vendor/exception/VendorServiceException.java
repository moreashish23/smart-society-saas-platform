package com.smartsociety.vendor.exception;

public class VendorServiceException extends RuntimeException {

    private final String errorCode;

    public VendorServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}