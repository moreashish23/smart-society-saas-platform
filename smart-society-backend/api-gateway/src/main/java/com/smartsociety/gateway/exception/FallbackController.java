package com.smartsociety.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        return buildFallback("auth-service", "Authentication service is temporarily unavailable.");
    }

    @RequestMapping(value = "/society", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<ResponseEntity<Map<String, Object>>> societyFallback() {
        return buildFallback("society-service", "Society service is temporarily unavailable.");
    }

    @RequestMapping(value = "/complaint", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<ResponseEntity<Map<String, Object>>> complaintFallback() {
        return buildFallback("complaint-service", "Complaint service is temporarily unavailable.");
    }

    @RequestMapping(value = "/vendor", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<ResponseEntity<Map<String, Object>>> vendorFallback() {
        return buildFallback("vendor-service", "Vendor service is temporarily unavailable.");
    }

    @RequestMapping(value = "/notification", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        return buildFallback("notification-service", "Notification service is temporarily unavailable.");
    }

    @RequestMapping(value = "/analytics", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<ResponseEntity<Map<String, Object>>> analyticsFallback() {
        return buildFallback("analytics-service", "Analytics service is temporarily unavailable.");
    }

    @RequestMapping(value = "/audit", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public Mono<ResponseEntity<Map<String, Object>>> auditFallback() {
        return buildFallback("audit-service", "Audit service is temporarily unavailable.");
    }

    private Mono<ResponseEntity<Map<String, Object>>> buildFallback(String service, String message) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success",   false,
                        "service",   service,
                        "message",   message,
                        "timestamp", LocalDateTime.now().toString()
                )));
    }
}