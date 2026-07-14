package com.smartsociety.society.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException() { super("User is already a member of this society"); }
}