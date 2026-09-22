package com.example.agecalculator.service;

import com.example.agecalculator.model.AgeCalculationResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
public class AgeCalculatorService {

    public AgeCalculationResponse calculateAge(LocalDate birthDate, LocalDate targetDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date cannot be null.");
        }
        if (targetDate == null) {
            targetDate = LocalDate.now();
        }
        if (birthDate.isAfter(targetDate)) {
            throw new IllegalArgumentException("Birth date cannot be in the future relative to the target date.");
        }

        Period period = Period.between(birthDate, targetDate);
        long totalDays = ChronoUnit.DAYS.between(birthDate, targetDate);
        long totalHours = totalDays * 24;
        long totalMinutes = totalHours * 60;
        long totalSeconds = totalMinutes * 60;

        // Next birthday calculation
        LocalDate nextBirthday = birthDate.withYear(targetDate.getYear());
        if (nextBirthday.isBefore(targetDate) || nextBirthday.isEqual(targetDate)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        long daysUntilNextBirthday = ChronoUnit.DAYS.between(targetDate, nextBirthday);
        Period nextBirthdayPeriod = Period.between(targetDate, nextBirthday);
        String nextBirthdayCountdown = String.format("%d months, %d days",
                nextBirthdayPeriod.getMonths(), nextBirthdayPeriod.getDays());

        // Day of week born
        String dayOfWeekBorn = birthDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // Zodiac sign
        String zodiacSign = determineZodiacSign(birthDate.getMonthValue(), birthDate.getDayOfMonth());

        return new AgeCalculationResponse(
                period.getYears(),
                period.getMonths(),
                period.getDays(),
                totalDays,
                totalHours,
                totalMinutes,
                totalSeconds,
                nextBirthdayCountdown,
                (int) daysUntilNextBirthday,
                dayOfWeekBorn,
                zodiacSign
        );
    }

    private String determineZodiacSign(int month, int day) {
        return switch (month) {
            case 1 -> (day <= 19) ? "Capricorn ♑" : "Aquarius ♒";
            case 2 -> (day <= 18) ? "Aquarius ♒" : "Pisces ♓";
            case 3 -> (day <= 20) ? "Pisces ♓" : "Aries ♈";
            case 4 -> (day <= 19) ? "Aries ♈" : "Taurus ♉";
            case 5 -> (day <= 20) ? "Taurus ♉" : "Gemini ♊";
            case 6 -> (day <= 20) ? "Gemini ♊" : "Cancer ♋";
            case 7 -> (day <= 22) ? "Cancer ♋" : "Leo ♌";
            case 8 -> (day <= 22) ? "Leo ♌" : "Virgo ♍";
            case 9 -> (day <= 22) ? "Virgo ♍" : "Libra ♎";
            case 10 -> (day <= 22) ? "Libra ♎" : "Scorpio ♏";
            case 11 -> (day <= 21) ? "Scorpio ♏" : "Sagittarius ♐";
            case 12 -> (day <= 21) ? "Sagittarius ♐" : "Capricorn ♑";
            default -> "Unknown";
        };
    }
}
