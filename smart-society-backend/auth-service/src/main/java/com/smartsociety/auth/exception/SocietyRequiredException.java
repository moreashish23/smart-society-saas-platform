package com.smartsociety.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SocietyRequiredException extends RuntimeException {
    public SocietyRequiredException() {
        super("societyId is required for this role");
    }
}