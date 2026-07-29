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
        List<Job> pendingJobs = jobRepository.findTop10ByStatusOrderByCreatedAtAsc(JobStatus.PENDING);

        for (Job job : pendingJobs.stream().limit(batchSize).toList()) {
            processJob(job);
        }
    }

    private void processJob(Job job) {
        try {
            jobStateMachine.markRunning(job);
            jobRepository.save(job);

            JobExecutor.ExecutionResult result = jobExecutor.execute(job);
            jobStateMachine.markSucceeded(job, result.output());
            jobRepository.save(job);

            log.info("Job {} succeeded in {}ms", job.getId(), result.durationMs());
        } catch (Exception ex) {
            log.error("Job {} failed: {}", job.getId(), ex.getMessage());
            jobExecutor.appendLog(job, "ERROR: " + ex.getMessage());
            JobStatus nextStatus = jobStateMachine.markFailed(job, ex.getMessage());
            jobRepository.save(job);

            if (nextStatus == JobStatus.DEAD_LETTER) {
                log.warn("Job {} moved to dead letter queue after {} attempts", job.getId(), job.getAttempts());
            }
        }
    }
}
