package com.smartsociety.society.dto.response;

import com.smartsociety.society.entity.NoticeStatus;
import com.smartsociety.society.entity.NoticeType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponse {
    private UUID          id;
    private UUID          societyId;
    private String        societyName;
    private String        title;
    private String        content;
    private NoticeType    noticeType;
    private NoticeStatus  status;
    private Boolean       priority;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private UUID          createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}