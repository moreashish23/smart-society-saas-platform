package com.smartsociety.complaint.exception;

public class CommentNotFoundException extends ComplaintServiceException {

    public CommentNotFoundException() {
        super(
                "Comment not found",
                "COMMENT_NOT_FOUND"
        );
    }
}