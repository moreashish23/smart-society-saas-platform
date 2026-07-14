package com.smartsociety.vendor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRatingException extends RuntimeException {
    public DuplicateRatingException() { super("Vendor has already been rated for this complaint"); }
}