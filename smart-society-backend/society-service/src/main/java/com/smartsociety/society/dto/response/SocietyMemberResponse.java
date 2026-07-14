package com.smartsociety.society.dto.response;

import com.smartsociety.society.entity.MemberStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocietyMemberResponse {
    private UUID          id;
    private UUID          societyId;
    private String        societyName;
    private UUID          userId;
    private String        role;
    private String        flatNumber;
    private String        block;
    private Integer       floor;
    private MemberStatus  status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime createdAt;
}