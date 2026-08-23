package com.flowforge.core.service;

import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final JobRepository jobRepository;

    public Map<String, Long> getStatsForUser(String userId) {
        Instant now = Instant.now();
        long pending = jobRepository.countByUserIdAndStatus(userId, JobStatus.PENDING);
        long scheduled = jobRepository.countByUserIdAndStatusAndScheduledAtAfter(
                userId, JobStatus.PENDING, now);

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", jobRepository.countByUserId(userId));
        stats.put("pending", pending - scheduled);
        stats.put("scheduled", scheduled);
        stats.put("running", jobRepository.countByUserIdAndStatus(userId, JobStatus.RUNNING));
        stats.put("succeeded", jobRepository.countByUserIdAndStatus(userId, JobStatus.SUCCEEDED));
        stats.put("failed", jobRepository.countByUserIdAndStatus(userId, JobStatus.FAILED));
        stats.put("deadLetter", jobRepository.countByUserIdAndStatus(userId, JobStatus.DEAD_LETTER));
        stats.put("cancelled", jobRepository.countByUserIdAndStatus(userId, JobStatus.CANCELLED));
        return stats;
    }
}
