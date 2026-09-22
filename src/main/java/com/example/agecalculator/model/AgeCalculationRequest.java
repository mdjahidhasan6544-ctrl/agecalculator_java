package com.example.agecalculator.model;

import java.time.LocalDate;

public record AgeCalculationRequest(
    LocalDate birthDate,
    LocalDate targetDate
) {}
