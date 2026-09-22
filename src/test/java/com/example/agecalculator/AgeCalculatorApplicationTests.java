package com.example.agecalculator;

import com.example.agecalculator.model.AgeCalculationResponse;
import com.example.agecalculator.service.AgeCalculatorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AgeCalculatorApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgeCalculatorService ageCalculatorService;

    @Test
    void contextLoads() {
        assertNotNull(ageCalculatorService);
    }

    @Test
    @DisplayName("Should accurately calculate age between two dates")
    void testExactAgeCalculation() {
        LocalDate birthDate = LocalDate.of(2000, 1, 15);
        LocalDate targetDate = LocalDate.of(2024, 6, 20);

        AgeCalculationResponse res = ageCalculatorService.calculateAge(birthDate, targetDate);

        assertEquals(24, res.years());
        assertEquals(5, res.months());
        assertEquals(5, res.days());
        assertEquals("Capricorn ♑", res.zodiacSign());
        assertEquals("Saturday", res.dayOfWeekBorn());
        assertTrue(res.totalDays() > 8000);
    }

    @Test
    @DisplayName("Should throw exception if birth date is in the future")
    void testFutureBirthDateThrowsException() {
        LocalDate birthDate = LocalDate.of(2030, 1, 1);
        LocalDate targetDate = LocalDate.of(2024, 1, 1);

        assertThrows(IllegalArgumentException.class, () ->
                ageCalculatorService.calculateAge(birthDate, targetDate));
    }

    @Test
    @DisplayName("Should verify API health check endpoint")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Should verify API calculate endpoint")
    void testCalculateEndpoint() throws Exception {
        mockMvc.perform(get("/api/calculate")
                        .param("birthDate", "1995-05-10")
                        .param("targetDate", "2025-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.years").value(30))
                .andExpect(jsonPath("$.zodiacSign").value("Taurus ♉"));
    }
}
