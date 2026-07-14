package com.smartsociety.complaint.service;

import com.smartsociety.complaint.dto.request.*;
import com.smartsociety.complaint.dto.response.*;
import com.smartsociety.complaint.entity.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ComplaintService {

    ComplaintResponse createComplaint(CreateComplaintRequest request, UUID residentId, UUID societyId);

    ComplaintResponse getComplaint(UUID complaintId, UUID societyId);

    PagedResponse<ComplaintResponse> getComplaints(UUID societyId, ComplaintStatus status,
                                                   ComplaintCategory category, ComplaintPriority priority,
                                                   String keyword, Pageable pageable);

    PagedResponse<ComplaintResponse> getMyComplaints(UUID residentId, UUID societyId, Pageable pageable);

    PagedResponse<ComplaintResponse> getAssignedComplaints(UUID assignedToId, Pageable pageable);

    ComplaintResponse assignComplaint(UUID complaintId, AssignComplaintRequest request,
                                      UUID managerId, UUID societyId);

    ComplaintResponse acceptComplaint(UUID complaintId, UUID vendorId, UUID societyId);

    ComplaintResponse startWork(UUID complaintId, UUID vendorId, UUID societyId);

    ComplaintResponse markWorkCompleted(UUID complaintId, UUID vendorId, UUID societyId);

    ComplaintResponse markResolved(UUID complaintId, ResolveComplaintRequest request,
                                   UUID managerId, UUID societyId);

    ComplaintResponse verifyResolution(UUID complaintId, boolean resolved,
                                       UUID residentId, UUID societyId);

    ComplaintResponse cancelComplaint(UUID complaintId, UUID requesterId, UUID societyId);


    CommentResponse addComment(UUID complaintId, AddCommentRequest request,
                               UUID authorId, String authorRole, UUID societyId);

    List<CommentResponse> getComments(UUID complaintId, UUID societyId);
}