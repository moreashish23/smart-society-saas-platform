package com.smartsociety.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.smartsociety.auth.config.AppProperties;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.smartsociety.auth.client")
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class AuthServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}