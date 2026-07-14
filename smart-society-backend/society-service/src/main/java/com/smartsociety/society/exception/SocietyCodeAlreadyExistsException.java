package com.smartsociety.society.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SocietyCodeAlreadyExistsException extends RuntimeException {
    public SocietyCodeAlreadyExistsException(String code) { super("Society code already exists: " + code); }
}