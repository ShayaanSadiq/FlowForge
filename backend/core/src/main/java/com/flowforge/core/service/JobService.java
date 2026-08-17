package com.flowforge.core.service;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.domain.JobType;
import com.flowforge.core.dto.CreateJobRequest;
import com.flowforge.core.dto.JobResponse;
import com.flowforge.core.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

import static com.flowforge.core.util.UserFacingMessages.JOB_NOT_FOUND;
import static com.flowforge.core.util.UserFacingMessages.RETRY_NOT_ALLOWED;
import static com.flowforge.core.util.UserFacingMessages.deprecatedJobType;
import static com.flowforge.core.util.UserFacingMessages.unsupportedStatusFilter;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final AuditService auditService;

    public JobResponse createJob(String userId, CreateJobRequest request) {
        if (request.getType() != null && request.getType().isDeprecated()) {
            throw new IllegalArgumentException(deprecatedJobType(request.getType().name()));
        }

        Job job = Job.builder()
                .userId(userId)
                .type(request.getType())
                .payload(request.getPayload())
                .status(JobStatus.PENDING)
                .scheduledAt(JobScheduling.resolveScheduledAt(request))
                .build();

        Job saved = jobRepository.save(job);
        auditService.log(userId, "CREATE", "JOB", saved.getId(), "Submitted job type " + request.getType());
        return toResponse(saved);
    }

    public Page<JobResponse> listJobs(String userId, String statusFilter, JobType type, Pageable pageable) {
        return findJobs(userId, statusFilter, type, pageable).map(this::toResponse);
    }

    private Page<Job> findJobs(String userId, String statusFilter, JobType type, Pageable pageable) {
        Instant now = Instant.now();
        boolean hasType = type != null;
        boolean hasStatus = statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter);

        if (hasStatus && "SCHEDULED".equalsIgnoreCase(statusFilter)) {
            return hasType
                    ? jobRepository.findScheduledByUserIdAndType(userId, now, type, pageable)
                    : jobRepository.findScheduledByUserId(userId, now, pageable);
        }

        if (hasStatus) {
            JobStatus status;
            try {
                status = JobStatus.valueOf(statusFilter.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(unsupportedStatusFilter(statusFilter));
            }
            return hasType
                    ? jobRepository.findByUserIdAndStatusAndType(userId, status, type, pageable)
                    : jobRepository.findByUserIdAndStatus(userId, status, pageable);
        }

        return hasType
                ? jobRepository.findByUserIdAndType(userId, type, pageable)
                : jobRepository.findByUserId(userId, pageable);
    }

    public JobResponse getJob(String userId, String jobId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new NoSuchElementException(JOB_NOT_FOUND));
        return toResponse(job);
    }

    public JobResponse retryJob(String userId, String jobId) {
        Job job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new NoSuchElementException(JOB_NOT_FOUND));

        if (job.getStatus() != JobStatus.FAILED && job.getStatus() != JobStatus.DEAD_LETTER) {
            throw new IllegalStateException(RETRY_NOT_ALLOWED);
        }

        job.setStatus(JobStatus.PENDING);
        job.setAttempts(0);
        job.setScheduledAt(Instant.now());
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
                .scheduledAt(job.getScheduledAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .build();
    }
}
