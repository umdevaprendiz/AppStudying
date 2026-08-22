package com.example.AppStudying;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AppStudyingApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppStudyingApplication.class, args);
	}

}
