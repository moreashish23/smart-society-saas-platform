package com.smartsociety.complaint.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;


@FeignClient(
        name = "vendor-service",
        url  = "${app.services.vendor-service-url}",
        configuration = VendorClient.VendorClientConfig.class,
        fallback = VendorClient.VendorClientFallback.class
)
public interface VendorClient {

    @PostMapping("/api/vendors/jobs")
    void createJob(@RequestBody CreateJobRequest request);

    class VendorClientConfig {
        @Bean
        public RequestInterceptor vendorClientHeaderPropagationInterceptor() {
            return (RequestTemplate template) -> {
                if (RequestContextHolder.getRequestAttributes()
                        instanceof ServletRequestAttributes sra) {
                    HttpServletRequest request = sra.getRequest();
                    propagate(template, request, "X-User-Id");
                    propagate(template, request, "X-User-Role");
                    propagate(template, request, "X-Society-Id");
                }
            };
        }

        private void propagate(RequestTemplate template, HttpServletRequest request, String header) {
            String value = request.getHeader(header);
            if (value != null) {
                template.header(header, value);
            }
        }
    }

    @lombok.extern.slf4j.Slf4j
    @org.springframework.stereotype.Component
    class VendorClientFallback implements VendorClient {
        @Override
        public void createJob(CreateJobRequest request) {
            log.warn("Vendor service unavailable — job not recorded: userId={}, complaintId={}",
                    request.getUserId(), request.getComplaintId());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class CreateJobRequest {
        private UUID userId;
        private UUID complaintId;
        private String complaintTitle;
        private String notes;
    }
}