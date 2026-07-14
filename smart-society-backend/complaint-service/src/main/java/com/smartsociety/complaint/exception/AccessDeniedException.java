package com.smartsociety.complaint.exception;

public class AccessDeniedException extends ComplaintServiceException {

    public AccessDeniedException() {
        super(
                "You do not have permission to perform this action",
                "ACCESS_DENIED"
        );
    }
}