package com.verav.travelagency.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilsTest {

    @Test
    void acceptsValidCustomerInput() {
        assertNull(ValidationUtils.validateCustomerInput(
                "Maria",
                "Papadopoulou",
                "maria@example.com",
                "+30 210 555 0101",
                "Greece"
        ));
    }

    @Test
    void rejectsInvalidCustomerEmail() {
        assertEquals(
                "Please enter a valid email address.",
                ValidationUtils.validateCustomerInput("Maria", "Papadopoulou", "maria", "+30 210 555 0101", "Greece")
        );
    }

    @Test
    void acceptsValidTripInput() {
        assertNull(ValidationUtils.validateTripInput(
                "Santorini",
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 20),
                "680.00",
                "18"
        ));
    }

    @Test
    void rejectsTripReturnDateBeforeDeparture() {
        assertEquals(
                "Return date cannot be before the departure date.",
                ValidationUtils.validateTripInput(
                        "Santorini",
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 15),
                        "680.00",
                        "18"
                )
        );
    }

    @Test
    void rejectsNegativeTripSeats() {
        assertEquals(
                "Available seats cannot be negative.",
                ValidationUtils.validateTripInput(
                        "Santorini",
                        LocalDate.of(2026, 6, 15),
                        LocalDate.of(2026, 6, 20),
                        "680.00",
                        "-1"
                )
        );
    }

    @Test
    void validatesIsoDates() {
        assertTrue(ValidationUtils.isValidIsoDate("2026-05-26"));
        assertFalse(ValidationUtils.isValidIsoDate("26/05/2026"));
    }
}
