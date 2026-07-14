package com.smartsociety.vendor.dto.request;

import com.smartsociety.vendor.entity.ServiceCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVendorRequest {

    @NotNull(message = "Society ID is required")
    private UUID            userId;

    @NotBlank (message = "Business name is required")
    private String          businessName;

    @NotBlank(message = "Contact person is required")
    private String          contactPerson;

    @NotBlank(message = "Contact email is required")
    @Email private String   contactEmail;

    @NotBlank(message = "Contact phone is required")
    private String          contactPhone;

    @NotNull (message = "Service category is required")
    private ServiceCategory serviceCategory;

    private String description;

    private String address;

}