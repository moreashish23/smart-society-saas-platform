package com.smartsociety.society.dto.request;

import com.smartsociety.society.entity.NoticeType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateNoticeRequest {
    @NotBlank(message = "Title is required") @Size(max = 255) private String title;
    @NotBlank(message = "Content is required") private String content;
    private NoticeType noticeType;
    private Boolean priority;
    private Boolean publishImmediately;
    private LocalDateTime expiresAt;
}