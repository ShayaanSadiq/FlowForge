package com.flowforge.core.dto;

import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.domain.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class JobResponse {

    private String id;
    private JobType type;
    private String payload;
    private JobStatus status;
    private int attempts;
    private int maxAttempts;
    private List<String> logs;
    private String result;
    private String errorMessage;
    private Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
}
