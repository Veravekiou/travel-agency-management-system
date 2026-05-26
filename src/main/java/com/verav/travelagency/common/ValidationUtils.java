package com.verav.travelagency.common;

import java.time.LocalDate;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static String validateCustomerInput(String firstName, String lastName, String email, String phone, String country) {
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(phone) || isBlank(country)) {
            return "Please fill in all customer fields.";
        }
        if (!email.contains("@") || !email.contains(".")) {
            return "Please enter a valid email address.";
        }
        return null;
    }

    public static String validateTripInput(String destination, LocalDate departureDate, LocalDate returnDate, String price, String seats) {
        if (isBlank(destination) || departureDate == null || returnDate == null || isBlank(price) || isBlank(seats)) {
            return "Please fill in all trip fields.";
        }
        if (returnDate.isBefore(departureDate)) {
            return "Return date cannot be before the departure date.";
        }

        try {
            double parsedPrice = Double.parseDouble(price.trim());
            if (parsedPrice <= 0) {
                return "Price must be greater than zero.";
            }
        } catch (NumberFormatException e) {
            return "Price must be a valid number.";
        }

        try {
            int parsedSeats = Integer.parseInt(seats.trim());
            if (parsedSeats < 0) {
                return "Available seats cannot be negative.";
            }
        } catch (NumberFormatException e) {
            return "Available seats must be a whole number.";
        }

        return null;
    }

    public static boolean isValidIsoDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
