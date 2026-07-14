package com.smartsociety.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private Jwt jwt = new Jwt();
    private PasswordReset passwordReset = new PasswordReset();
    private Services services = new Services();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs  = 900_000L;
        private long refreshTokenExpirationMs = 604_800_000L;
    }

    @Getter
    @Setter
    public static class PasswordReset {
        private long tokenExpirationMs = 900_000L;
        private String frontendUrl     = "http://localhost:5173";
    }

    @Getter
    @Setter
    public static class Services {
        private String auditServiceUrl = "http://localhost:8088";
    }
}