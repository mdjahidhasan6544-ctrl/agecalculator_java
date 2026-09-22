package com.example.agecalculator.model;

public record AgeCalculationResponse(
    int years,
    int months,
    int days,
    long totalDays,
    long totalHours,
    long totalMinutes,
    long totalSeconds,
    String nextBirthdayCountdown,
    int daysUntilNextBirthday,
    String dayOfWeekBorn,
    String zodiacSign
) {}
