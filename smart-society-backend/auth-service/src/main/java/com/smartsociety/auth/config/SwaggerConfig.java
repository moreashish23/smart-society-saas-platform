package com.smartsociety.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Society — Auth Service")
                        .description("Authentication & Authorization API: registration, login, JWT, refresh tokens, password management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Smart Society Platform")
                                .email("dev@smartsociety.com"))
                        .license(new License().name("Private")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Dev"),
                        new Server().url("http://api-gateway:8080").description("Via API Gateway")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT access token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}