package com.flowforge.core.dto;

import com.flowforge.core.domain.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateJobRequest {

    @NotNull
    private JobType type;

    @NotBlank
    private String payload;
}
