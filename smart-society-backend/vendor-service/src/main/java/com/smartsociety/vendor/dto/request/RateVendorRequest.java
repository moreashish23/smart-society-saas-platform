package com.smartsociety.vendor.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateVendorRequest {
    @NotNull (message = "Complaint ID cannot be null")
    private UUID       complaintId;

    @NotNull (message = "Rating cannot be null")
    @DecimalMin("1.0")
    @DecimalMax("5.0")
    private BigDecimal rating;

    private String review;
}