package com.flowforge.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
@CompoundIndex(name = "status_scheduledAt_idx", def = "{ 'status': 1, 'scheduledAt': 1 }")
public class Job {

    @Id
    private String id;

    @Indexed
    private String userId;

    private JobType type;

    private String payload;

    @Indexed
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Builder.Default
    private int attempts = 0;

    @Builder.Default
    private int maxAttempts = 3;

    @Builder.Default
    private List<String> logs = new ArrayList<>();

    private String result;

    private String errorMessage;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant startedAt;

    private Instant finishedAt;

    /** When the worker should pick up this job. Defaults to immediate execution. */
    @Indexed
    private Instant scheduledAt;
}
