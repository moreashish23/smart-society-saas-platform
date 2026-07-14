package com.smartsociety.society.dto.request;

import com.smartsociety.society.entity.SubscriptionPlan;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSocietyRequest {
    @NotBlank(message = "Name is Required")
    @Size(max = 255) private String name;

    @NotBlank(message = "Code is Required")
    @Size(max = 50)  private String code;

    private String description;

    @NotBlank(message = "Address Line  is Required")
    @Size(max = 255) private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is Required")
    @Size(max = 100) private String city;

    @NotBlank(message = "State is Required")
    @Size(max = 100) private String state;

    @NotBlank(message = "Pincode is Required")
    @Size(max = 20)  private String pincode;

    private String country;

    @NotBlank(message = "Contact Email is Required")
    @Email           private String contactEmail;

    @NotBlank(message = "Contact Phone is Required")
    @Size(max = 20)  private String contactPhone;

    @Min(0) private Integer totalUnits;

    @Min(0) private Integer totalFloors;

    private SubscriptionPlan subscriptionPlan;

    private String logoUrl;
}