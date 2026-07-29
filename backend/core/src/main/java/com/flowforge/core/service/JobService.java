package com.flowforge.core.service;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.dto.CreateJobRequest;
import com.flowforge.core.dto.JobResponse;
import com.flowforge.core.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final AuditService auditService;

    public JobResponse createJob(String userId, CreateJobRequest request) {
        Job job = Job.builder()
                .userId(userId)
                .type(request.getType())
                .payload(request.getPayload())
                .status(JobStatus.PENDING)
                .build();

        Job saved = jobRepository.save(job);
        auditService.log(userId, "CREATE", "JOB", saved.getId(), "Submitted job type " + request.getType());
        return toResponse(saved);
    }

    public Page<JobResponse> listJobs(String userId, Pageable pageable) {
        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public JobResponse getJob(String userId, String jobId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));
        return toResponse(job);
    }

    public JobResponse retryJob(String userId, String jobId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));

        if (job.getStatus() != JobStatus.FAILED && job.getStatus() != JobStatus.DEAD_LETTER) {
            throw new IllegalStateException("Only failed or dead-letter jobs can be retried");
        }

        job.setStatus(JobStatus.PENDING);
        job.setAttempts(0);
        job.setErrorMessage(null);
        job.setResult(null);
        job.setStartedAt(null);
        job.setFinishedAt(null);
        job.getLogs().clear();

        Job saved = jobRepository.save(job);
        auditService.log(userId, "RETRY", "JOB", saved.getId(), "Manual retry requested");
        return toResponse(saved);
    }

    public JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .type(job.getType())
                .payload(job.getPayload())
                .status(job.getStatus())
                .attempts(job.getAttempts())
                .maxAttempts(job.getMaxAttempts())
                .logs(job.getLogs())
                .result(job.getResult())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }
}
