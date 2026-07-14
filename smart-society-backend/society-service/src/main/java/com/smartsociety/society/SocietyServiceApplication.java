package com.smartsociety.society;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.smartsociety.society.client")
@EnableAsync
public class SocietyServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(SocietyServiceApplication.class, args);
	}
}