package com.smartsociety.complaint.controller;

import com.smartsociety.complaint.repository.ComplaintRepository;
import com.smartsociety.complaint.entity.ComplaintStatus;
import com.smartsociety.complaint.entity.ComplaintCategory;
import com.smartsociety.complaint.entity.ComplaintPriority;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Complaint Analytics",
        description = "Internal analytics endpoints — returns plain POJOs (no ApiResponse wrapper) so Feign deserialises directly")
public class ComplaintAnalyticsController {

    private final ComplaintRepository complaintRepository;


    private static final List<ComplaintStatus> TERMINAL =
            List.of(ComplaintStatus.CLOSED, ComplaintStatus.CANCELLED);

    @GetMapping("/summary")
    @Operation(summary = "Complaint KPI summary [Internal — analytics-service Feign]")
    public ComplaintSummaryResponse getSummary(@RequestParam("societyId") UUID societyId) {

        long open                = complaintRepository.countBySocietyIdAndStatus(societyId, ComplaintStatus.OPEN);
        long assigned            = complaintRepository.countBySocietyIdAndStatus(societyId, ComplaintStatus.ASSIGNED);
        long inProgress          = complaintRepository.countBySocietyIdAndStatus(societyId, ComplaintStatus.IN_PROGRESS);
        long pendingVerification = complaintRepository.countBySocietyIdAndStatus(societyId, ComplaintStatus.PENDING_VERIFICATION);
        long closed              = complaintRepository.countBySocietyIdAndStatus(societyId, ComplaintStatus.CLOSED);
        long cancelled           = complaintRepository.countBySocietyIdAndStatus(societyId, ComplaintStatus.CANCELLED);
        long reopened            = complaintRepository.countBySocietyIdAndStatus(societyId, ComplaintStatus.REOPENED);

        long critical = complaintRepository.countBySocietyIdAndPriority(societyId, ComplaintPriority.CRITICAL);
        long high     = complaintRepository.countBySocietyIdAndPriority(societyId, ComplaintPriority.HIGH);
        long medium   = complaintRepository.countBySocietyIdAndPriority(societyId, ComplaintPriority.MEDIUM);
        long low      = complaintRepository.countBySocietyIdAndPriority(societyId, ComplaintPriority.LOW);


        long slaBreaches = complaintRepository.countSlaBreaches(societyId, TERMINAL);

        Double avgResolution = complaintRepository.avgResolutionHours(societyId);

        long total = open + assigned + inProgress + pendingVerification
                + closed + cancelled + reopened;

        return ComplaintSummaryResponse.builder()
                .total(total).open(open).assigned(assigned).inProgress(inProgress)
                .pendingVerification(pendingVerification).closed(closed)
                .cancelled(cancelled).reopened(reopened)
                .critical(critical).high(high).medium(medium).low(low)
                .slaBreaches(slaBreaches)
                .avgResolutionHours(avgResolution != null ? avgResolution : 0.0)
                .build();
    }

    @GetMapping("/trend")
    @Operation(summary = "Daily complaint trend [Internal — analytics-service Feign]")
    public ComplaintTrendResponse getTrend(
            @RequestParam("societyId") UUID societyId,
            @RequestParam(value = "days", defaultValue = "30") int days) {

        LocalDate endDate   = LocalDate.now();
        LocalDate startDate = endDate.minusDays(Math.min(days, 365));
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<ComplaintTrendResponse.DailyCount> daily = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd   = current.plusDays(1).atStartOfDay();

            daily.add(ComplaintTrendResponse.DailyCount.builder()
                    .date(current.format(fmt))
                    .created(complaintRepository.countCreatedBetween(societyId, dayStart, dayEnd))
                    .closed(complaintRepository.countClosedBetween(societyId, dayStart, dayEnd))
                    .escalated(complaintRepository.countEscalatedBetween(societyId, dayStart, dayEnd))
                    .build());

            current = current.plusDays(1);
        }

        return ComplaintTrendResponse.builder().daily(daily).build();
    }



    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplaintSummaryResponse {
        private long total, open, assigned, inProgress, pendingVerification;
        private long closed, cancelled, reopened;
        private long critical, high, medium, low;
        private long slaBreaches;
        private double avgResolutionHours;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplaintTrendResponse {
        private List<DailyCount> daily;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DailyCount {
            private String date;
            private long created, closed, escalated;
        }
    }
}