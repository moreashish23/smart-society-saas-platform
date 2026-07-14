package com.smartsociety.vendor.controller;

import com.smartsociety.vendor.entity.VendorStatus;
import com.smartsociety.vendor.repository.VendorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendors/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Analytics", description = "Internal analytics endpoints — NOT wrapped in ApiResponse so Feign can deserialize directly")
public class VendorAnalyticsController {

    private final VendorRepository vendorRepository;

    @GetMapping("/performance")
    @Operation(summary = "Get vendor performance for a society [Internal — used by analytics-service Feign]")
    public List<VendorPerformanceResponse> getVendorPerformance(
            @RequestParam("societyId") UUID societyId) {

        List<VendorPerformanceResponse> result = vendorRepository
                .findBySocietyIdAndStatus(societyId, VendorStatus.ACTIVE,
                        PageRequest.of(0, 100))
                .getContent()
                .stream()
                .map(v -> VendorPerformanceResponse.builder()
                        .vendorId(v.getId())
                        .businessName(v.getBusinessName())
                        .serviceCategory(v.getServiceCategory().name())
                        .totalJobs(v.getTotalJobs())
                        .completedJobs(v.getCompletedJobs())
                        .rating(v.getRating())
                        .build())
                .collect(Collectors.toList());

        log.debug("Vendor performance for societyId={}: {} vendors", societyId, result.size());
        return result;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VendorPerformanceResponse {
        private UUID vendorId;
        private String businessName;
        private String serviceCategory;
        private Integer totalJobs;
        private Integer completedJobs;
        private BigDecimal rating;
    }
}