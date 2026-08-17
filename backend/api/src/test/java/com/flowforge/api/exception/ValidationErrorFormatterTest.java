package com.flowforge.api.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationErrorFormatterTest {

    @Test
    void mapsJobFieldsToPlainLanguage() {
        assertThat(ValidationErrorFormatter.formatFieldError("type", "must not be null"))
                .isEqualTo("Please select a job type.");
        assertThat(ValidationErrorFormatter.formatFieldError("payload", "must not be blank"))
                .isEqualTo("Job input cannot be empty.");
    }

    @Test
    void mapsAuthFieldsToPlainLanguage() {
        assertThat(ValidationErrorFormatter.formatFieldError("email", "must not be blank"))
                .isEqualTo("Email is required.");
        assertThat(ValidationErrorFormatter.formatFieldError("password", "size must be between 8 and 2147483647"))
                .isEqualTo("Password must be at least 8 characters.");
        assertThat(ValidationErrorFormatter.formatFieldError("displayName", "must not be blank"))
                .isEqualTo("Display name is required.");
    }
}
