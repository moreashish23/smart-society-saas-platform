package com.smartsociety.vendor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VendorAlreadyExistsException extends RuntimeException {
    public VendorAlreadyExistsException() { super("Vendor already registered for this society"); }
}