package com.smartsociety.audit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.audit")
@Getter
@Setter
public class AppProperties {

    private int retentionDays = 365;
}