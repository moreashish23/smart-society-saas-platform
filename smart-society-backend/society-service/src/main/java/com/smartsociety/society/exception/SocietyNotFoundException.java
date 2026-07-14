package com.smartsociety.society.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SocietyNotFoundException extends RuntimeException {
    public SocietyNotFoundException(String id) { super("Society not found: " + id); }
}