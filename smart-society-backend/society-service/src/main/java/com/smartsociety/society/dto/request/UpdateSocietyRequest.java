package com.smartsociety.society.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSocietyRequest {

    @Size(max = 50)
    private String code;

    @Size(max = 255)
    private String name;

    private String description;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String pincode;

    private String country;

    @Email
    private String contactEmail;

    @Size(max = 20)
    private String contactPhone;

    @Min(0)
    private Integer totalUnits;

    @Min(0)
    private Integer totalFloors;

    private String logoUrl;
}