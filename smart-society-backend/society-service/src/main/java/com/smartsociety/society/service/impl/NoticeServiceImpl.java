package com.smartsociety.society.service.impl;

import com.smartsociety.society.client.AuditClient;
import com.smartsociety.society.client.NotificationClient;
import com.smartsociety.society.dto.request.CreateNoticeRequest;
import com.smartsociety.society.dto.request.UpdateNoticeRequest;
import com.smartsociety.society.dto.response.NoticeResponse;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.entity.*;
import com.smartsociety.society.exception.NoticeNotFoundException;
import com.smartsociety.society.exception.SocietyNotFoundException;
import com.smartsociety.society.mapper.NoticeMapper;
import com.smartsociety.society.repository.NoticeRepository;
import com.smartsociety.society.repository.SocietyRepository;
import com.smartsociety.society.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository   noticeRepository;
    private final SocietyRepository  societyRepository;
    private final NoticeMapper       noticeMapper;
    private final AuditClient        auditClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public NoticeResponse createNotice(UUID societyId, CreateNoticeRequest request, UUID createdBy) {
        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> new SocietyNotFoundException(societyId.toString()));

        Notice notice = noticeMapper.toEntity(request);
        notice.setSociety(society);
        notice.setCreatedBy(createdBy);
        notice.setPriority(request.getPriority() != null && request.getPriority());

        if (Boolean.TRUE.equals(request.getPublishImmediately())) {
            notice.publish();
        }

        Notice saved = noticeRepository.save(notice);

        audit(createdBy, societyId, saved.getId(), "NOTICE_CREATE",
                "Notice created: " + saved.getTitle());

        log.info("Notice created: id={}, societyId={}, type={}", saved.getId(), societyId, saved.getNoticeType());
        return noticeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse getNotice(UUID societyId, UUID noticeId) {
        Notice notice = noticeRepository.findByIdAndSocietyId(noticeId, societyId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId.toString()));
        return noticeMapper.toResponse(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NoticeResponse> getNotices(UUID societyId, NoticeStatus status,
                                                    NoticeType type, Pageable pageable) {
        var page = (status != null)
                ? noticeRepository.findBySocietyIdAndStatus(societyId, status, pageable)
                : (type != null)
                ? noticeRepository.findBySocietyIdAndNoticeType(societyId, type, pageable)
                : noticeRepository.findBySocietyId(societyId, pageable);

        return PagedResponse.of(page.map(noticeMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeResponse> getActiveNotices(UUID societyId) {
        return noticeRepository.findActiveNotices(societyId)
                .stream()
                .map(noticeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoticeResponse updateNotice(UUID societyId, UUID noticeId,
                                       UpdateNoticeRequest request, UUID requesterId) {
        Notice notice = findInSociety(noticeId, societyId);
        noticeMapper.updateFromRequest(request, notice);
        Notice updated = noticeRepository.save(notice);
        audit(requesterId, societyId, noticeId, "NOTICE_UPDATE",
                "Notice updated: " + updated.getTitle());
        return noticeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public NoticeResponse publishNotice(UUID societyId, UUID noticeId, UUID requesterId) {
        Notice notice = findInSociety(noticeId, societyId);
        notice.publish();
        Notice updated = noticeRepository.save(notice);

        audit(requesterId, societyId, noticeId, "NOTICE_PUBLISH",
                "Notice published: " + updated.getTitle());


        notifyAsync(NotificationClient.NotificationRequest.builder()
                .societyId(societyId)
                .recipientId(null)
                .type("NOTICE_PUBLISHED")
                .title("New Notice: " + updated.getTitle())
                .message(updated.getContent().length() > 120
                        ? updated.getContent().substring(0, 120) + "…"
                        : updated.getContent())
                .entityId(noticeId)
                .entityType("NOTICE")
                .build());

        log.info("Notice published: id={}", noticeId);
        return noticeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public NoticeResponse archiveNotice(UUID societyId, UUID noticeId, UUID requesterId) {
        Notice notice = findInSociety(noticeId, societyId);
        notice.archive();
        Notice updated = noticeRepository.save(notice);
        audit(requesterId, societyId, noticeId, "NOTICE_ARCHIVE",
                "Notice archived: " + updated.getTitle());
        log.info("Notice archived: id={}", noticeId);
        return noticeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteNotice(UUID societyId, UUID noticeId, UUID requesterId) {
        Notice notice = findInSociety(noticeId, societyId);
        noticeRepository.delete(notice);
        audit(requesterId, societyId, noticeId, "NOTICE_DELETE",
                "Notice deleted: " + notice.getTitle());
        log.info("Notice deleted: id={}", noticeId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Notice findInSociety(UUID noticeId, UUID societyId) {
        return noticeRepository.findByIdAndSocietyId(noticeId, societyId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId.toString()));
    }

    @Async
    void audit(UUID userId, UUID societyId, UUID entityId, String action, String description) {
        try {
            auditClient.logEvent(AuditClient.AuditEventRequest.builder()
                    .userId(userId).societyId(societyId).action(action)
                    .entityType("NOTICE").entityId(entityId).description(description)
                    .build());
        } catch (Exception ex) {
            log.warn("Audit event failed: {}", ex.getMessage());
        }
    }


    @Async
    void notifyAsync(NotificationClient.NotificationRequest request) {
        try {
            notificationClient.sendNotification(request);
        } catch (Exception ex) {
            log.warn("Notification failed: {}", ex.getMessage());
        }
    }
}