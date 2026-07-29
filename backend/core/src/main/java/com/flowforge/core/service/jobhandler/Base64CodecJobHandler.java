package com.flowforge.core.service.jobhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowforge.core.domain.Job;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;

@Component
public class Base64CodecJobHandler {

    private final JobPayloadParser payloadParser;

    public Base64CodecJobHandler(JobPayloadParser payloadParser) {
        this.payloadParser = payloadParser;
    }

    public String execute(Job job, Consumer<String> log) {
        JsonNode config = payloadParser.parseJson(job.getPayload());
        String operation = payloadParser.optionalText(config, "operation", "encode").toLowerCase();
        String text = payloadParser.requiredText(config, "text");

        return switch (operation) {
            case "encode" -> {
                log.accept("Base64 encoding " + text.length() + " chars");
                yield Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
            }
            case "decode" -> {
                log.accept("Base64 decoding");
                try {
                    byte[] decoded = Base64.getDecoder().decode(text.trim());
                    yield new String(decoded, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException ex) {
                    throw new JobHandlerException("Invalid Base64 input");
                }
            }
            default -> throw new JobHandlerException("operation must be 'encode' or 'decode'");
        };
    }
}
