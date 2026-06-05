package com.ubs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(UbsApplication.class, args);
	}

}
