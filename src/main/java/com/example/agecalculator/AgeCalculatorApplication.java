package com.example.agecalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application entrypoint for the Age Calculator SPA.
 * Serves static assets from src/main/resources/static/ and provides
 * REST API support for age calculation.
 */
@SpringBootApplication
public class AgeCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgeCalculatorApplication.class, args);
    }
}
