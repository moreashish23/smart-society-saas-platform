package com.smartsociety.complaint.repository;

import com.smartsociety.complaint.entity.ComplaintAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, UUID> {
    List<ComplaintAttachment> findByComplaintId(UUID complaintId);
}