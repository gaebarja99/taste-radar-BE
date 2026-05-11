package com.tasteradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class TasteRadarApplication {

	public static void main(String[] args) {
		SpringApplication.run(TasteRadarApplication.class, args);
	}
}
