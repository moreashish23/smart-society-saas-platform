package com.smartsociety.complaint.exception;

public class InvalidStatusTransitionException extends ComplaintServiceException {

    public InvalidStatusTransitionException(String from, String to) {
        super(
                "Cannot transition complaint from " + from + " to " + to,
                "INVALID_STATUS_TRANSITION"
        );
    }
}