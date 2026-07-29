package com.flowforge.core.service;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobResult;
import com.flowforge.core.repository.JobResultRepository;
import com.flowforge.core.service.jobhandler.Base64CodecJobHandler;
import com.flowforge.core.service.jobhandler.CsvAnalyzeJobHandler;
import com.flowforge.core.service.jobhandler.HashGenerateJobHandler;
import com.flowforge.core.service.jobhandler.HttpRequestJobHandler;
import com.flowforge.core.service.jobhandler.JobHandlerException;
import com.flowforge.core.service.jobhandler.JsonFormatJobHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutor {

    private final JobResultRepository jobResultRepository;
    private final PythonScriptRunner pythonScriptRunner;
    private final HttpRequestJobHandler httpRequestJobHandler;
    private final JsonFormatJobHandler jsonFormatJobHandler;
    private final CsvAnalyzeJobHandler csvAnalyzeJobHandler;
    private final HashGenerateJobHandler hashGenerateJobHandler;
    private final Base64CodecJobHandler base64CodecJobHandler;

    public ExecutionResult execute(Job job) throws InterruptedException {
        Instant start = Instant.now();
        appendLog(job, "Starting job execution for type: " + job.getType());

        String output = switch (job.getType()) {
            case PYTHON_SCRIPT -> runPythonScript(job);
            case HTTP_REQUEST -> runWithHandler(job, httpRequestJobHandler::execute);
            case JSON_FORMAT -> runWithHandler(job, jsonFormatJobHandler::execute);
            case CSV_ANALYZE -> runWithHandler(job, csvAnalyzeJobHandler::execute);
            case HASH_GENERATE -> runWithHandler(job, hashGenerateJobHandler::execute);
            case BASE64_CODEC -> runWithHandler(job, base64CodecJobHandler::execute);
            case SIMULATION -> runSimulation(job);
            case DATA_TRANSFORM -> runDataTransform(job);
            case REPORT_GENERATION -> runReportGeneration(job);
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

    @FunctionalInterface
    private interface JobHandler {
        String run(Job job, Consumer<String> log) throws Exception;
    }

    private String runWithHandler(Job job, JobHandler handler) throws InterruptedException {
        try {
            return handler.run(job, line -> appendLog(job, line));
        } catch (JobHandlerException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Job failed: " + ex.getMessage(), ex);
        }
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
        appendLog(job, "Running Python script");
        try {
            PythonScriptRunner.PythonRunResult result = pythonScriptRunner.run(
                    job.getPayload(),
                    line -> appendLog(job, line));

            String output = result.stdout().isBlank()
                    ? "(no stdout)"
                    : result.stdout();

            if (!result.stderr().isBlank()) {
                appendLog(job, "stderr captured (" + result.stderr().lines().count() + " lines)");
            }

            return output;
        } catch (PythonScriptRunner.PythonExecutionException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to execute Python script: " + ex.getMessage(), ex);
        }
    }

    public void appendLog(Job job, String message) {
        String entry = Instant.now() + " | " + message;
        job.getLogs().add(entry);
        log.info("[Job {}] {}", job.getId(), message);
    }

    public record ExecutionResult(String output, long durationMs) {}
}
