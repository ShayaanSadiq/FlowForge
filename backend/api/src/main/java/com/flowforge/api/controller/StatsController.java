package com.flowforge.api.controller;

import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final JobRepository jobRepository;

    @GetMapping
    public Map<String, Long> getStats() {
        Instant now = Instant.now();
        long pending = jobRepository.countByStatus(JobStatus.PENDING);
        long scheduled = jobRepository.countByStatusAndScheduledAtAfter(JobStatus.PENDING, now);
        return Map.of(
                "pending", pending - scheduled,
                "scheduled", scheduled,
                "running", jobRepository.countByStatus(JobStatus.RUNNING),
                "succeeded", jobRepository.countByStatus(JobStatus.SUCCEEDED),
                "failed", jobRepository.countByStatus(JobStatus.FAILED),
                "deadLetter", jobRepository.countByStatus(JobStatus.DEAD_LETTER)
        );
    }
}
