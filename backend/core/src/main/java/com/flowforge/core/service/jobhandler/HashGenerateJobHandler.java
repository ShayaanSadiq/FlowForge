package com.flowforge.core.service.jobhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowforge.core.domain.Job;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class HashGenerateJobHandler {

    private static final Set<String> ALLOWED = Set.of("SHA-256", "SHA-512");

    private final JobPayloadParser payloadParser;

    public HashGenerateJobHandler(JobPayloadParser payloadParser) {
        this.payloadParser = payloadParser;
    }

    public String execute(Job job, Consumer<String> log) throws Exception {
        HashRequest request = parseRequest(job.getPayload());

        if (!ALLOWED.contains(request.algorithm())) {
            throw new JobHandlerException("Supported algorithms: SHA-256, SHA-512");
        }

        if ("lines".equals(request.mode())) {
            return hashLines(request, log);
        }

        String hash = computeHash(request.text(), request.algorithm());
        log.accept("Computed " + request.algorithm() + " hash");
        log.accept("Input length: " + request.text().length() + " chars");

        String result = request.algorithm() + ":" + hash;
        if (request.expected() != null) {
            boolean match = hash.equalsIgnoreCase(request.expected());
            log.accept(match ? "Hash verification: MATCH" : "Hash verification: MISMATCH");
            result += "\nverification=" + (match ? "MATCH" : "MISMATCH");
        }
        return result;
    }

    private HashRequest parseRequest(String payload) {
        String trimmed = payload.trim();
        if (!trimmed.startsWith("{")) {
            return new HashRequest(trimmed, "SHA-256", "single", null);
        }

        JsonNode config = payloadParser.parseJson(trimmed);
        String text = payloadParser.requiredText(config, "text");
        String algorithm = payloadParser.optionalText(config, "algorithm", "SHA-256").toUpperCase(Locale.ROOT);
        String mode = payloadParser.optionalText(config, "mode", "single").toLowerCase(Locale.ROOT);
        String expected = config.has("expected") && !config.get("expected").isNull()
                ? config.get("expected").asText().trim()
                : null;

        if (!Set.of("single", "lines").contains(mode)) {
            throw new JobHandlerException("mode must be 'single' or 'lines'");
        }
        if (expected != null) {
            expected = expected.contains(":") ? expected.substring(expected.indexOf(':') + 1) : expected;
        }

        return new HashRequest(text, algorithm, mode, expected);
    }

    private String hashLines(HashRequest request, Consumer<String> log) throws Exception {
        String[] lines = request.text().split("\\R");
        log.accept("Hashing " + lines.length + " lines with " + request.algorithm());

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String hash = computeHash(line, request.algorithm());
            log.accept("Line " + (i + 1) + ": " + hash);
            if (!output.isEmpty()) {
                output.append('\n');
            }
            output.append(line).append(" => ").append(request.algorithm()).append(':').append(hash);
        }

        if (output.isEmpty()) {
            throw new JobHandlerException("No non-empty lines to hash");
        }
        return output.toString();
    }

    private String computeHash(String text, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private record HashRequest(String text, String algorithm, String mode, String expected) {}
}
