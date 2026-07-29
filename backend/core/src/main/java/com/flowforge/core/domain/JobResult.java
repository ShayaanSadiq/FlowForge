package com.flowforge.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "job_results")
public class JobResult {

    @Id
    private String id;

    @Indexed
    private String jobId;

    private String output;

    @Builder.Default
    private Map<String, Object> metrics = new HashMap<>();

    @CreatedDate
    private Instant createdAt;
}
