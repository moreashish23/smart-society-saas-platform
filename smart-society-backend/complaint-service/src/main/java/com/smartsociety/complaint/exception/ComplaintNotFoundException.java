package com.smartsociety.complaint.exception;

public class ComplaintNotFoundException extends ComplaintServiceException {

    public ComplaintNotFoundException(String id) {
        super(
                "Complaint not found: " + id,
                "COMPLAINT_NOT_FOUND"
        );
    }
}