package com.smartsociety.complaint.scheduler;

import com.smartsociety.complaint.client.NotificationClient;
import com.smartsociety.complaint.entity.Complaint;
import com.smartsociety.complaint.entity.ComplaintStatus;
import com.smartsociety.complaint.entity.TimelineAction;
import com.smartsociety.complaint.entity.ComplaintTimeline;
import com.smartsociety.complaint.repository.ComplaintRepository;
import com.smartsociety.complaint.repository.ComplaintTimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EscalationScheduler {

    private final ComplaintRepository         complaintRepository;
    private final ComplaintTimelineRepository timelineRepository;
    private final NotificationClient          notificationClient;

    private static final UUID SYSTEM_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");


    private static final List<ComplaintStatus> TERMINAL_STATUSES =
            List.of(ComplaintStatus.CLOSED, ComplaintStatus.CANCELLED);

    @Scheduled(cron = "${app.escalation.check-interval-cron:0 */30 * * * *}")
    @Transactional
    public void runEscalation() {
        LocalDateTime now = LocalDateTime.now();

        List<Complaint> breached =
                complaintRepository.findBreachedComplaintsForEscalation(now, TERMINAL_STATUSES);

        if (breached.isEmpty()) {
            log.debug("Escalation check: no breached complaints found at {}", now);
            return;
        }

        log.info("Escalation check: {} breached complaints found at {}", breached.size(), now);

        for (Complaint complaint : breached) {
            try {
                escalate(complaint);
            } catch (Exception ex) {
                log.error("Failed to escalate complaint {}: {}",
                        complaint.getId(), ex.getMessage());
            }
        }
    }

    private void escalate(Complaint complaint) {
        int previousLevel = complaint.getEscalationLevel();
        complaint.escalate();           // increments escalationLevel, sets escalatedAt
        complaintRepository.save(complaint);

        int newLevel = complaint.getEscalationLevel();
        String targetRole = resolveEscalationTarget(newLevel);

        ComplaintTimeline entry = ComplaintTimeline.builder()
                .complaint(complaint)
                .action(TimelineAction.ESCALATED)
                .performedBy(SYSTEM_USER_ID)
                .note("Auto-escalated to Level " + newLevel + " (" + targetRole
                        + ") — SLA breached")
                .build();
        timelineRepository.save(entry);

        try {
            notificationClient.sendNotification(
                    NotificationClient.NotificationRequest.builder()
                            .societyId(complaint.getSocietyId())
                            .type("COMPLAINT_ESCALATED")
                            .title("⚠️ Complaint Escalated to " + targetRole)
                            .message(String.format(
                                    "Complaint '%s' [%s priority] has breached SLA and been " +
                                            "escalated to Level %d. Immediate attention required.",
                                    complaint.getTitle(), complaint.getPriority(), newLevel))
                            .entityId(complaint.getId())
                            .entityType("COMPLAINT")
                            .build());
        } catch (Exception ex) {
            log.warn("Escalation notification failed for complaint {}: {}",
                    complaint.getId(), ex.getMessage());
        }

        log.info("Complaint escalated: id={}, level {} → {}, target={}",
                complaint.getId(), previousLevel, newLevel, targetRole);
    }

    private String resolveEscalationTarget(int level) {
        return switch (level) {
            case 1 -> "SOCIETY_MANAGER";
            case 2 -> "COMMITTEE_MEMBER";
            case 3 -> "SUPER_ADMIN";
            default -> "UNKNOWN";
        };
    }
}