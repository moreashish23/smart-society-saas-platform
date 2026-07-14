package com.smartsociety.society.service.impl;

import com.smartsociety.society.client.AuditClient;
import com.smartsociety.society.dto.request.AddMemberRequest;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.dto.response.SocietyMemberResponse;
import com.smartsociety.society.entity.*;
import com.smartsociety.society.exception.*;
import com.smartsociety.society.mapper.SocietyMemberMapper;
import com.smartsociety.society.repository.SocietyMemberRepository;
import com.smartsociety.society.repository.SocietyRepository;
import com.smartsociety.society.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final SocietyRepository       societyRepository;
    private final SocietyMemberRepository memberRepository;
    private final SocietyMemberMapper     memberMapper;
    private final AuditClient             auditClient;

    @Override
    @Transactional
    public SocietyMemberResponse addMember(UUID societyId, AddMemberRequest request, UUID requesterId) {
        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> new SocietyNotFoundException(societyId.toString()));

        if (memberRepository.existsBySocietyIdAndUserId(societyId, request.getUserId())) {
            throw new MemberAlreadyExistsException();
        }

        SocietyMember member = SocietyMember.builder()
                .society(society)
                .userId(request.getUserId())
                .role(request.getRole())
                .flatNumber(request.getFlatNumber())
                .block(request.getBlock())
                .floor(request.getFloor())
                .status(MemberStatus.ACTIVE)
                .build();

        SocietyMember saved = memberRepository.save(member);

        audit(requesterId, societyId, saved.getId(), "MEMBER_ADD",
                "Member added: userId=" + request.getUserId() + ", role=" + request.getRole());

        log.info("Member added: societyId={}, userId={}, role={}", societyId, request.getUserId(), request.getRole());
        return memberMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SocietyMemberResponse getMember(UUID societyId, UUID memberId) {
        SocietyMember member = memberRepository.findById(memberId)
                .filter(m -> m.getSociety().getId().equals(societyId))
                .orElseThrow(MemberNotFoundException::new);
        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SocietyMemberResponse> getMembers(UUID societyId, String role,
                                                           MemberStatus status, Pageable pageable) {
        Page<SocietyMemberResponse> page = memberRepository
                .findMembers(societyId, role, status, pageable)
                .map(memberMapper::toResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional
    public SocietyMemberResponse updateMemberRole(UUID societyId, UUID memberId,
                                                  String newRole, UUID requesterId) {
        SocietyMember member = memberRepository.findById(memberId)
                .filter(m -> m.getSociety().getId().equals(societyId))
                .orElseThrow(MemberNotFoundException::new);

        String oldRole = member.getRole();
        member.setRole(newRole);
        SocietyMember updated = memberRepository.save(member);

        audit(requesterId, societyId, memberId, "MEMBER_ROLE_UPDATE",
                "Member role updated: userId=" + member.getUserId() + ", " + oldRole + " -> " + newRole);

        log.info("Member role updated: memberId={}, {} -> {}", memberId, oldRole, newRole);
        return memberMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void removeMember(UUID societyId, UUID memberId, UUID requesterId) {
        SocietyMember member = memberRepository.findById(memberId)
                .filter(m -> m.getSociety().getId().equals(societyId))
                .orElseThrow(MemberNotFoundException::new);

        member.deactivate();
        memberRepository.save(member);

        audit(requesterId, societyId, memberId, "MEMBER_REMOVE",
                "Member removed: userId=" + member.getUserId());

        log.info("Member removed: societyId={}, memberId={}", societyId, memberId);
    }

    @Async
    void audit(UUID userId, UUID societyId, UUID entityId, String action, String description) {
        try {
            auditClient.logEvent(AuditClient.AuditEventRequest.builder()
                    .userId(userId).societyId(societyId).action(action)
                    .entityType("SOCIETY_MEMBER").entityId(entityId).description(description)
                    .build());
        } catch (Exception ex) {
            log.warn("Audit event failed: {}", ex.getMessage());
        }
    }
}