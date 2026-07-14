package com.smartsociety.complaint.util;

import com.smartsociety.complaint.entity.ComplaintPriority;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConfigurationProperties(prefix = "app.sla")
@Getter @Setter
public class SlaProperties {

    private int criticalHours = 4;
    private int highHours     = 24;
    private int mediumHours   = 48;
    private int lowHours      = 72;


    public LocalDateTime computeDeadline(ComplaintPriority priority) {
        LocalDateTime now = LocalDateTime.now();
        return switch (priority) {
            case CRITICAL -> now.plusHours(criticalHours);
            case HIGH     -> now.plusHours(highHours);
            case MEDIUM   -> now.plusHours(mediumHours);
            case LOW      -> now.plusHours(lowHours);
        };
    }


    public int getHoursForPriority(ComplaintPriority priority) {
        return switch (priority) {
            case CRITICAL -> criticalHours;
            case HIGH     -> highHours;
            case MEDIUM   -> mediumHours;
            case LOW      -> lowHours;
        };
    }
}