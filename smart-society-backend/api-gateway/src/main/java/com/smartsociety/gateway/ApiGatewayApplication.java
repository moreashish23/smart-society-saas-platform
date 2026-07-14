package com.smartsociety.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.smartsociety.gateway.config.GatewayAppProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayAppProperties.class)
public class ApiGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}