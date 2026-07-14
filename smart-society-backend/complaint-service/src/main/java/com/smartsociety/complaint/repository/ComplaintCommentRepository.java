package com.smartsociety.complaint.repository;

import com.smartsociety.complaint.entity.ComplaintComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintCommentRepository extends JpaRepository<ComplaintComment, UUID> {
    List<ComplaintComment> findByComplaintIdOrderByCreatedAtAsc(UUID complaintId);
    Optional<ComplaintComment> findByIdAndComplaintId(UUID id, UUID complaintId);
}