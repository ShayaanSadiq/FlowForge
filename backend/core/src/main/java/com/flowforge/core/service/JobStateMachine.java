package com.flowforge.core.service;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import org.springframework.stereotype.Component;

@Component
public class JobStateMachine {

    public void markRunning(Job job) {
        validateTransition(job.getStatus(), JobStatus.RUNNING);
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(java.time.Instant.now());
    }

    public void markSucceeded(Job job, String result) {
        validateTransition(job.getStatus(), JobStatus.SUCCEEDED);
        job.setStatus(JobStatus.SUCCEEDED);
        job.setResult(result);
        job.setFinishedAt(java.time.Instant.now());
        job.setErrorMessage(null);
    }

    public JobStatus markFailed(Job job, String errorMessage) {
        validateTransition(job.getStatus(), JobStatus.FAILED);
        job.setAttempts(job.getAttempts() + 1);
        job.setErrorMessage(errorMessage);

        if (job.getAttempts() >= job.getMaxAttempts()) {
            job.setStatus(JobStatus.DEAD_LETTER);
            job.setFinishedAt(java.time.Instant.now());
            return JobStatus.DEAD_LETTER;
        }

        job.setStatus(JobStatus.PENDING);
        job.setStartedAt(null);
        return JobStatus.PENDING;
    }

    public void retry(Job job) {
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
    }

    private void validateTransition(JobStatus current, JobStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == JobStatus.RUNNING;
            case RUNNING -> next == JobStatus.SUCCEEDED || next == JobStatus.FAILED;
            case FAILED -> next == JobStatus.FAILED || next == JobStatus.PENDING;
            case SUCCEEDED, DEAD_LETTER -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Invalid transition from " + current + " to " + next);
        }
    }
}
