package com.smartsociety.vendor.dto.request;

import com.smartsociety.vendor.entity.ServiceCategory;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVendorRequest {
    private String          businessName;
    private String          contactPerson;

    @Email(message = "Invalid email format")
    private String   contactEmail;

    private String          contactPhone;
    private ServiceCategory serviceCategory;
    private String          description;
    private String          address;
}