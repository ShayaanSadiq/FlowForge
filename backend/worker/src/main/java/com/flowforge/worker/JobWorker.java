package com.flowforge.worker;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.repository.JobRepository;
import com.flowforge.core.service.JobExecutor;
import com.flowforge.core.service.JobStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobWorker {

    private final JobRepository jobRepository;
    private final JobStateMachine jobStateMachine;
    private final JobExecutor jobExecutor;

    @Value("${flowforge.worker.batch-size:10}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${flowforge.worker.poll-interval-ms:2000}")
    public void pollAndProcessJobs() {
        List<Job> pendingJobs = jobRepository.findTop10ReadyJobsOrderByScheduledAtAscCreatedAtAsc(
                JobStatus.PENDING, Instant.now());

        for (Job job : pendingJobs.stream().limit(batchSize).toList()) {
            processJob(job);
        }
    }

    private void processJob(Job job) {
        Job current = jobRepository.findById(job.getId()).orElse(null);
        if (current == null || current.getStatus() != JobStatus.PENDING) {
            return;
        }

        try {
            jobStateMachine.markRunning(current);
            jobRepository.save(current);

            JobExecutor.ExecutionResult result = jobExecutor.execute(current);
            jobStateMachine.markSucceeded(current, result.output());
            jobRepository.save(current);

            log.info("Job {} succeeded in {}ms", current.getId(), result.durationMs());
        } catch (Exception ex) {
            log.error("Job {} failed: {}", current.getId(), ex.getMessage());
            jobExecutor.appendLog(current, "ERROR: " + ex.getMessage());
            JobStatus nextStatus = jobStateMachine.markFailed(current, ex.getMessage());
            jobRepository.save(current);

            if (nextStatus == JobStatus.DEAD_LETTER) {
                log.warn("Job {} moved to dead letter queue after {} attempts", current.getId(), current.getAttempts());
            }
        }
    }
}
