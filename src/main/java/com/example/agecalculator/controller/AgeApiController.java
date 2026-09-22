package com.example.agecalculator.controller;

import com.example.agecalculator.model.AgeCalculationRequest;
import com.example.agecalculator.model.AgeCalculationResponse;
import com.example.agecalculator.service.AgeCalculatorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AgeApiController {

    private final AgeCalculatorService ageCalculatorService;

    public AgeApiController(AgeCalculatorService ageCalculatorService) {
        this.ageCalculatorService = ageCalculatorService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "age-calculator"));
    }

    @GetMapping("/calculate")
    public ResponseEntity<?> calculateAgeGet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        try {
            AgeCalculationResponse response = ageCalculatorService.calculateAge(birthDate, targetDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculateAgePost(@RequestBody AgeCalculationRequest request) {
        try {
            AgeCalculationResponse response = ageCalculatorService.calculateAge(request.birthDate(), request.targetDate());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
