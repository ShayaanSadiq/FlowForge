package com.flowforge.api.controller;

import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final JobRepository jobRepository;

    @GetMapping
    public Map<String, Long> getStats() {
        return Map.of(
                "pending", jobRepository.countByStatus(JobStatus.PENDING),
                "running", jobRepository.countByStatus(JobStatus.RUNNING),
                "succeeded", jobRepository.countByStatus(JobStatus.SUCCEEDED),
                "failed", jobRepository.countByStatus(JobStatus.FAILED),
                "deadLetter", jobRepository.countByStatus(JobStatus.DEAD_LETTER)
        );
    }
}
