package com.smartsociety.complaint.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Society — Complaint Service")
                        .description("Complaint lifecycle management: creation, assignment, SLA tracking, escalation, and resident verification")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Direct"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")));
    }
}