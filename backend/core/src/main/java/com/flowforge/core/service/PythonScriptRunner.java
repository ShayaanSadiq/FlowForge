package com.flowforge.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
public class PythonScriptRunner {

    @Value("${flowforge.python.executable:python3}")
    private String pythonExecutable;

    @Value("${flowforge.python.timeout-seconds:30}")
    private long timeoutSeconds;

    @Value("${flowforge.python.max-script-chars:100000}")
    private int maxScriptChars;

    @Value("${flowforge.python.max-output-chars:65536}")
    private int maxOutputChars;

    public PythonRunResult run(String script, Consumer<String> logConsumer) throws IOException, InterruptedException {
        validateScript(script);

        Path tempDir = Files.createTempDirectory("flowforge-py-");
        Path scriptFile = tempDir.resolve("script.py");

        try {
            Files.writeString(scriptFile, script, StandardCharsets.UTF_8);
            logConsumer.accept("Writing script to " + scriptFile);
            logConsumer.accept("Executing: " + pythonExecutable + " " + scriptFile.getFileName());

            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, scriptFile.toString());
            processBuilder.directory(tempDir.toFile());
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            StringBuilder stdoutBuilder = new StringBuilder();
            StringBuilder stderrBuilder = new StringBuilder();

            Thread stdoutThread = Thread.ofVirtual().start(() ->
                    drainStream(process.getInputStream(), stdoutBuilder, logConsumer, "stdout"));
            Thread stderrThread = Thread.ofVirtual().start(() ->
                    drainStream(process.getErrorStream(), stderrBuilder, logConsumer, "stderr"));

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            stdoutThread.join(5_000);
            stderrThread.join(5_000);

            if (!finished) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                throw new PythonExecutionException("Script timed out after " + timeoutSeconds + " seconds");
            }

            int exitCode = process.exitValue();
            String stdout = stdoutBuilder.toString();
            String stderr = stderrBuilder.toString();

            logConsumer.accept("Process exited with code " + exitCode);

            if (exitCode != 0) {
                throw new PythonExecutionException(
                        "Script failed with exit code " + exitCode + (stderr.isBlank() ? "" : ": " + stderr.trim()));
            }

            return new PythonRunResult(stdout, stderr, exitCode);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private void validateScript(String script) {
        if (script == null || script.isBlank()) {
            throw new PythonExecutionException("Python script payload cannot be empty");
        }
        if (script.length() > maxScriptChars) {
            throw new PythonExecutionException("Script exceeds maximum size of " + maxScriptChars + " characters");
        }
    }

    private void drainStream(InputStream stream, StringBuilder output, Consumer<String> logConsumer, String label) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                synchronized (output) {
                    if (output.length() + line.length() + 1 > maxOutputChars) {
                        logConsumer.accept(label + ": output truncated at " + maxOutputChars + " characters");
                        break;
                    }
                    logConsumer.accept(label + " | " + line);
                    if (!output.isEmpty()) {
                        output.append('\n');
                    }
                    output.append(line);
                }
            }
        } catch (IOException ex) {
            logConsumer.accept(label + " | read error: " + ex.getMessage());
        }
    }

    private void deleteDirectory(Path directory) {
        try (var paths = Files.walk(directory)) {
            paths.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    log.warn("Failed to delete {}: {}", path, ex.getMessage());
                }
            });
        } catch (IOException ex) {
            log.warn("Failed to clean temp directory {}: {}", directory, ex.getMessage());
        }
    }

    public record PythonRunResult(String stdout, String stderr, int exitCode) {}

    public static class PythonExecutionException extends RuntimeException {
        public PythonExecutionException(String message) {
            super(message);
        }
    }
}
