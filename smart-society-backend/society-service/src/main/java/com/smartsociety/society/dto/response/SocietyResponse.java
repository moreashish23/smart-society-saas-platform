package com.smartsociety.society.dto.response;

import com.smartsociety.society.entity.SocietyStatus;
import com.smartsociety.society.entity.SubscriptionPlan;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocietyResponse {
    private UUID             id;
    private String           name;
    private String           code;
    private String           description;
    private String           addressLine1;
    private String           addressLine2;
    private String           city;
    private String           state;
    private String           pincode;
    private String           country;
    private String           contactEmail;
    private String           contactPhone;
    private Integer          totalUnits;
    private Integer          totalFloors;
    private SocietyStatus    status;
    private SubscriptionPlan subscriptionPlan;
    private LocalDateTime    subscriptionExpiry;
    private String           logoUrl;
    private UUID             createdBy;
    private Long             totalMembers;
    private LocalDateTime    createdAt;
    private LocalDateTime    updatedAt;
}