package com.smartsociety.society.service.impl;

import com.smartsociety.society.client.AuditClient;
import com.smartsociety.society.dto.request.CreateSocietyRequest;
import com.smartsociety.society.dto.request.UpdateSocietyRequest;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.dto.response.SocietyResponse;
import com.smartsociety.society.entity.Society;
import com.smartsociety.society.entity.SocietyStatus;
import com.smartsociety.society.exception.SocietyCodeAlreadyExistsException;
import com.smartsociety.society.exception.SocietyNotFoundException;
import com.smartsociety.society.mapper.SocietyMapper;
import com.smartsociety.society.repository.SocietyRepository;
import com.smartsociety.society.service.SocietyService;
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
public class SocietyServiceImpl implements SocietyService {

    private final SocietyRepository societyRepository;
    private final SocietyMapper     societyMapper;
    private final AuditClient       auditClient;

    @Override
    @Transactional
    public SocietyResponse createSociety(CreateSocietyRequest request, UUID createdBy) {
        if (societyRepository.existsByCode(request.getCode())) {
            throw new SocietyCodeAlreadyExistsException(request.getCode());
        }

        Society society = societyMapper.toEntity(request);
        society.setCreatedBy(createdBy);

        Society saved = societyRepository.save(society);

        audit(createdBy, saved.getId(), saved.getId(), "SOCIETY_CREATE",
                "Society created: " + saved.getName());

        log.info("Society created: id={}, code={}", saved.getId(), saved.getCode());
        return toResponseWithCounts(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SocietyResponse getSociety(UUID societyId) {
        return toResponseWithCounts(findById(societyId));
    }

    @Override
    @Transactional
    public SocietyResponse updateSociety(UUID societyId, UpdateSocietyRequest request,
                                         UUID requesterId) {
        Society society = findById(societyId);

        if (request.getCode() != null
                && !request.getCode().equals(society.getCode())
                && societyRepository.existsByCodeAndIdNot(request.getCode(), societyId)) {
            throw new SocietyCodeAlreadyExistsException(request.getCode());
        }

        societyMapper.updateFromRequest(request, society);
        Society updated = societyRepository.save(society);

        audit(requesterId, societyId, societyId, "SOCIETY_UPDATE",
                "Society updated: " + updated.getName());

        log.info("Society updated: id={}", societyId);
        return toResponseWithCounts(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SocietyResponse> getAllSocieties(SocietyStatus status, String name,
                                                          Pageable pageable) {
        Page<SocietyResponse> page = societyRepository
                .searchSocieties(status, name, pageable)
                .map(this::toResponseWithCounts);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional
    public SocietyResponse activateSociety(UUID societyId, UUID requesterId) {
        Society society = findById(societyId);
        society.activate();
        Society updated = societyRepository.save(society);
        audit(requesterId, societyId, societyId, "SOCIETY_ACTIVATE",
                "Society activated: " + updated.getName());
        log.info("Society activated: id={}", societyId);
        return toResponseWithCounts(updated);
    }

    @Override
    @Transactional
    public SocietyResponse deactivateSociety(UUID societyId, UUID requesterId) {
        Society society = findById(societyId);
        society.deactivate();
        Society updated = societyRepository.save(society);
        audit(requesterId, societyId, societyId, "SOCIETY_DEACTIVATE",
                "Society deactivated: " + updated.getName());
        log.info("Society deactivated: id={}", societyId);
        return toResponseWithCounts(updated);
    }

    @Override
    @Transactional
    public void deleteSociety(UUID societyId, UUID requesterId) {
        Society society = findById(societyId);
        societyRepository.delete(society);
        audit(requesterId, societyId, societyId, "SOCIETY_DELETE",
                "Society deleted: " + society.getName());
        log.info("Society deleted: id={}", societyId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Society findById(UUID societyId) {
        return societyRepository.findById(societyId)
                .orElseThrow(() -> new SocietyNotFoundException(societyId.toString()));
    }

    private SocietyResponse toResponseWithCounts(Society society) {
        SocietyResponse response = societyMapper.toResponse(society);
        response.setTotalMembers(societyRepository.countActiveMembers(society.getId()));
        return response;
    }

    @Async
    void audit(UUID userId, UUID societyId, UUID entityId, String action, String description) {
        try {
            auditClient.logEvent(AuditClient.AuditEventRequest.builder()
                    .userId(userId).societyId(societyId)
                    .action(action).entityType("SOCIETY")
                    .entityId(entityId).description(description)
                    .build());
        } catch (Exception ex) {
            log.warn("Audit event failed: {}", ex.getMessage());
        }
    }
}