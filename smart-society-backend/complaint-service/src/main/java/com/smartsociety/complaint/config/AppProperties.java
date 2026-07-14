package com.smartsociety.complaint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Services services = new Services();

    @Getter @Setter
    public static class Services {
        private String vendorServiceUrl;
        private String notificationServiceUrl;
        private String auditServiceUrl;
    }
}