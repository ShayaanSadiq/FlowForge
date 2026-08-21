package com.flowforge.core.dto;

import com.flowforge.core.domain.JobType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateJobRequest {

    @NotNull
    private JobType type;

    @NotBlank
    @Size(max = 500_000, message = "Payload exceeds the maximum allowed size")
    private String payload;

    /** Run after this many seconds from submission. Mutually exclusive with scheduledAt. */
    @Min(0)
    private Integer delaySeconds;

    /** Run at this UTC instant. Mutually exclusive with delaySeconds. */
    private Instant scheduledAt;
}
