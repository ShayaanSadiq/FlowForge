package com.flowforge.core.service;

import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private JobRepository jobRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(jobRepository);
    }

    @Test
    void returnsUserScopedCounts() {
        when(jobRepository.countByUserId("user-1")).thenReturn(12L);
        when(jobRepository.countByUserIdAndStatus("user-1", JobStatus.PENDING)).thenReturn(5L);
        when(jobRepository.countByUserIdAndStatusAndScheduledAtAfter(
                eq("user-1"), eq(JobStatus.PENDING), any(Instant.class))).thenReturn(2L);
        when(jobRepository.countByUserIdAndStatus("user-1", JobStatus.RUNNING)).thenReturn(1L);
        when(jobRepository.countByUserIdAndStatus("user-1", JobStatus.SUCCEEDED)).thenReturn(4L);
        when(jobRepository.countByUserIdAndStatus("user-1", JobStatus.FAILED)).thenReturn(0L);
        when(jobRepository.countByUserIdAndStatus("user-1", JobStatus.DEAD_LETTER)).thenReturn(0L);
        when(jobRepository.countByUserIdAndStatus("user-1", JobStatus.CANCELLED)).thenReturn(0L);

        Map<String, Long> stats = statsService.getStatsForUser("user-1");

        assertThat(stats.get("total")).isEqualTo(12L);
        assertThat(stats.get("pending")).isEqualTo(3L);
        assertThat(stats.get("scheduled")).isEqualTo(2L);
        assertThat(stats.get("running")).isEqualTo(1L);
        assertThat(stats.get("succeeded")).isEqualTo(4L);
    }
}
