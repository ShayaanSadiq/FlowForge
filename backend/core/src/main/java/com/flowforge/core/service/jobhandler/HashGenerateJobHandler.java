package com.flowforge.core.service.jobhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowforge.core.domain.Job;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
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
        String text;
        String algorithm;

        String trimmed = job.getPayload().trim();
        if (trimmed.startsWith("{")) {
            JsonNode config = payloadParser.parseJson(trimmed);
            text = payloadParser.requiredText(config, "text");
            algorithm = payloadParser.optionalText(config, "algorithm", "SHA-256").toUpperCase();
        } else {
            text = trimmed;
            algorithm = "SHA-256";
        }

        if (!ALLOWED.contains(algorithm)) {
            throw new JobHandlerException("Supported algorithms: SHA-256, SHA-512");
        }

        log.accept("Computing " + algorithm + " hash");
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        String hex = HexFormat.of().formatHex(hash);

        log.accept("Input length: " + text.length() + " chars");
        return algorithm + ":" + hex;
    }
}
