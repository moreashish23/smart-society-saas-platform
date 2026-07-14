package com.smartsociety.vendor.config;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Services services = new Services();

    @Getter
    @Setter
    public static class Services { private String auditServiceUrl; }
}