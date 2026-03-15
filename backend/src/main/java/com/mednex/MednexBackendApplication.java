package com.mednex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MednexBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MednexBackendApplication.class, args);
	}
}
