package com.shape.converter.kmltosdo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KmltosdoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KmltosdoApplication.class, args);
	}

}
