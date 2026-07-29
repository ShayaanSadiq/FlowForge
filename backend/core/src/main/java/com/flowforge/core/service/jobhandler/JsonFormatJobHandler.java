package com.flowforge.core.service.jobhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowforge.core.domain.Job;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class JsonFormatJobHandler {

    private final JobPayloadParser payloadParser;

    public JsonFormatJobHandler(JobPayloadParser payloadParser) {
        this.payloadParser = payloadParser;
    }

    public String execute(Job job, Consumer<String> log) {
        String rawJson = extractJson(job.getPayload());
        log.accept("Validating JSON (" + rawJson.length() + " chars)");

        String pretty = payloadParser.prettyJson(rawJson);
        log.accept("JSON is valid");
        return pretty;
    }

    private String extractJson(String payload) {
        String trimmed = payload.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return trimmed;
        }

        JsonNode config = payloadParser.parseJson(trimmed);
        return payloadParser.requiredText(config, "json");
    }
}
