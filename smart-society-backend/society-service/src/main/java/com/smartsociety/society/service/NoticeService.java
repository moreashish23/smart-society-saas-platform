package com.smartsociety.society.service;

import com.smartsociety.society.dto.request.CreateNoticeRequest;
import com.smartsociety.society.dto.request.UpdateNoticeRequest;
import com.smartsociety.society.dto.response.NoticeResponse;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.entity.NoticeStatus;
import com.smartsociety.society.entity.NoticeType;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface NoticeService {
    NoticeResponse             createNotice(UUID societyId, CreateNoticeRequest request, UUID createdBy);
    NoticeResponse             getNotice(UUID societyId, UUID noticeId);
    PagedResponse<NoticeResponse> getNotices(UUID societyId, NoticeStatus status, NoticeType type, Pageable pageable);
    List<NoticeResponse>       getActiveNotices(UUID societyId);
    NoticeResponse             updateNotice(UUID societyId, UUID noticeId, UpdateNoticeRequest request, UUID requesterId);
    NoticeResponse             publishNotice(UUID societyId, UUID noticeId, UUID requesterId);
    NoticeResponse             archiveNotice(UUID societyId, UUID noticeId, UUID requesterId);
    void                       deleteNotice(UUID societyId, UUID noticeId, UUID requesterId);
}