package com.smartsociety.auth.dto.response;

import com.smartsociety.auth.entity.AccountStatus;
import com.smartsociety.auth.entity.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID          id;
    private String        email;
    private String        firstName;
    private String        lastName;
    private String        fullName;
    private String        phone;
    private UserRole      role;
    private AccountStatus status;
    private UUID          societyId;
    private String        flatNumber;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}