package com.flowforge.api.exception;

final class ValidationErrorFormatter {

    private ValidationErrorFormatter() {
    }

    static String formatFieldError(String field, String message) {
        if (message == null || message.isBlank()) {
            return defaultForField(field);
        }

        return switch (field) {
            case "type" -> "Please select a job type.";
            case "payload" -> "Job input cannot be empty.";
            case "email" -> message.contains("must not be blank") || message.contains("must not be null")
                    ? "Email is required."
                    : "Please enter a valid email address.";
            case "password" -> message.toLowerCase().contains("size")
                    ? "Password must be at least 8 characters."
                    : "Password is required.";
            case "displayName" -> "Display name is required.";
            case "delaySeconds" -> "Delay must be zero or more seconds.";
            default -> defaultForField(field);
        };
    }

    private static String defaultForField(String field) {
        return switch (field) {
            case "type" -> "Please select a job type.";
            case "payload" -> "Job input cannot be empty.";
            case "email" -> "Email is required.";
            case "password" -> "Password is required.";
            case "displayName" -> "Display name is required.";
            case "delaySeconds" -> "Delay must be zero or more seconds.";
            default -> "Please check your input and try again.";
        };
    }
}
