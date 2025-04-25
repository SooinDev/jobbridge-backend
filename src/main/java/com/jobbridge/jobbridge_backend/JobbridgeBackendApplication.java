package com.jobbridge.jobbridge_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobbridgeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobbridgeBackendApplication.class, args);
	}

}