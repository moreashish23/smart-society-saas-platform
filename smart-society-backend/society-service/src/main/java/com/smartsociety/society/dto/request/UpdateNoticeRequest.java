package com.smartsociety.society.dto.request;

import com.smartsociety.society.entity.NoticeType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNoticeRequest {
    private String      title;
    private String      content;
    private NoticeType  noticeType;
    private Boolean     priority;
    private LocalDateTime expiresAt;
}