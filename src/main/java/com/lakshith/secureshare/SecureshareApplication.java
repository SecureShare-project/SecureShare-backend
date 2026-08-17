package com.lakshith.secureshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureshareApplication {

	public static void main(String[] args) {

		SpringApplication.run(SecureshareApplication.class, args);
	}

}
