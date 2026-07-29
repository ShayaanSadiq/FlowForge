package com.flowforge.core.service;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobResult;
import com.flowforge.core.repository.JobResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutor {

    private final JobResultRepository jobResultRepository;

    public ExecutionResult execute(Job job) throws InterruptedException {
        Instant start = Instant.now();
        appendLog(job, "Starting job execution for type: " + job.getType());

        String output = switch (job.getType()) {
            case SIMULATION -> runSimulation(job);
            case DATA_TRANSFORM -> runDataTransform(job);
            case REPORT_GENERATION -> runReportGeneration(job);
            case PYTHON_SCRIPT -> runPythonScript(job);
        };

        long durationMs = Duration.between(start, Instant.now()).toMillis();
        appendLog(job, "Job completed in " + durationMs + "ms");

        JobResult result = JobResult.builder()
                .jobId(job.getId())
                .output(output)
                .metrics(Map.of(
                        "durationMs", durationMs,
                        "attempt", job.getAttempts() + 1,
                        "type", job.getType().name()
                ))
                .build();
        jobResultRepository.save(result);

        return new ExecutionResult(output, durationMs);
    }

    private String runSimulation(Job job) throws InterruptedException {
        appendLog(job, "Running simulation with payload: " + job.getPayload());
        int steps = Math.clamp(job.getPayload().length() % 5 + 1, 1, 5);
        for (int i = 1; i <= steps; i++) {
            TimeUnit.MILLISECONDS.sleep(500);
            appendLog(job, "Simulation step " + i + "/" + steps + " complete");
        }
        return "Simulation finished with " + steps + " steps";
    }

    private String runDataTransform(Job job) throws InterruptedException {
        appendLog(job, "Transforming data: " + job.getPayload());
        TimeUnit.MILLISECONDS.sleep(1000);
        String transformed = job.getPayload().toUpperCase();
        appendLog(job, "Transform output length: " + transformed.length());
        return transformed;
    }

    private String runReportGeneration(Job job) throws InterruptedException {
        appendLog(job, "Generating report for: " + job.getPayload());
        TimeUnit.MILLISECONDS.sleep(1500);
        appendLog(job, "Report sections: summary, metrics, recommendations");
        return "Report generated for '" + job.getPayload() + "' at " + Instant.now();
    }

    private String runPythonScript(Job job) throws InterruptedException {
        appendLog(job, "Python script execution simulated (sandbox not enabled in MVP)");
        appendLog(job, "Script: " + job.getPayload());
        TimeUnit.MILLISECONDS.sleep(800);
        return "Simulated Python output: processed " + job.getPayload().split("\\s+").length + " tokens";
    }

    public void appendLog(Job job, String message) {
        String entry = Instant.now() + " | " + message;
        job.getLogs().add(entry);
        log.info("[Job {}] {}", job.getId(), message);
    }

    public record ExecutionResult(String output, long durationMs) {}
}
