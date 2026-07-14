package com.smartsociety.analytics.config;

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
    private final Cache cache = new Cache();

    @Getter
    @Setter
    public static class Services {
        private String complaintServiceUrl;
        private String vendorServiceUrl;
    }

    @Getter
    @Setter
    public static class Cache {
        private int dashboardTtlMinutes = 5;
    }
}