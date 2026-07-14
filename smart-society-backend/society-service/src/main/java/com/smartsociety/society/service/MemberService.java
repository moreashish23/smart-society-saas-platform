package com.smartsociety.society.service;

import com.smartsociety.society.dto.request.AddMemberRequest;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.dto.response.SocietyMemberResponse;
import com.smartsociety.society.entity.MemberStatus;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface MemberService {
    SocietyMemberResponse             addMember(UUID societyId, AddMemberRequest request, UUID requesterId);
    SocietyMemberResponse             getMember(UUID societyId, UUID memberId);
    PagedResponse<SocietyMemberResponse> getMembers(UUID societyId, String role, MemberStatus status, Pageable pageable);
    SocietyMemberResponse             updateMemberRole(UUID societyId, UUID memberId, String newRole, UUID requesterId);
    void                              removeMember(UUID societyId, UUID memberId, UUID requesterId);
}