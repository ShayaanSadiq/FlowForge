package com.flowforge.core.service;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.domain.JobType;
import com.flowforge.core.dto.JobResponse;
import com.flowforge.core.repository.AuditEventRepository;
import com.flowforge.core.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        AuditService auditService = new AuditService(mock(AuditEventRepository.class));
        jobService = new JobService(jobRepository, auditService);
    }

    @Test
    void listJobsUsesDefaultQueryWhenNoFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        Job job = Job.builder()
                .id("job-1")
                .userId("user-1")
                .type(JobType.PYTHON_SCRIPT)
                .payload("print('hi')")
                .status(JobStatus.SUCCEEDED)
                .build();
        when(jobRepository.findByUserId("user-1", pageable))
                .thenReturn(new PageImpl<>(List.of(job)));

        Page<JobResponse> result = jobService.listJobs("user-1", null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getType()).isEqualTo(JobType.PYTHON_SCRIPT);
        verify(jobRepository).findByUserId("user-1", pageable);
    }

    @Test
    void listJobsUsesScheduledQueryForScheduledFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        when(jobRepository.findScheduledByUserId(eq("user-1"), any(Instant.class), eq(pageable)))
                .thenReturn(Page.empty());

        jobService.listJobs("user-1", "SCHEDULED", null, pageable);

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(jobRepository).findScheduledByUserId(eq("user-1"), instantCaptor.capture(), eq(pageable));
        assertThat(instantCaptor.getValue()).isBeforeOrEqualTo(Instant.now().plusSeconds(1));
    }

    @Test
    void listJobsCombinesStatusAndTypeFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        when(jobRepository.findByUserIdAndStatusAndType(
                "user-1", JobStatus.FAILED, JobType.HASH_GENERATE, pageable))
                .thenReturn(Page.empty());

        jobService.listJobs("user-1", "FAILED", JobType.HASH_GENERATE, pageable);

        verify(jobRepository).findByUserIdAndStatusAndType(
                "user-1", JobStatus.FAILED, JobType.HASH_GENERATE, pageable);
    }

    @Test
    void listJobsRejectsUnknownStatusFilter() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> jobService.listJobs("user-1", "NOT_A_STATUS", null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown status filter");
    }
}
