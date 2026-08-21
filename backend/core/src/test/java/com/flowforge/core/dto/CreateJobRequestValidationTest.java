package com.flowforge.core.dto;

import com.flowforge.core.domain.JobType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateJobRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void rejectsOversizedPayload() {
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.JSON_FORMAT);
        request.setPayload("x".repeat(500_001));

        var violations = validator.validate(request);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("payload"));
    }

    @Test
    void acceptsPayloadWithinLimit() {
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.JSON_FORMAT);
        request.setPayload("{\"ok\":true}");

        assertThat(validator.validate(request)).isEmpty();
    }
}
