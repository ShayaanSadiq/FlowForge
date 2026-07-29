package com.flowforge.core.service.jobhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JobPayloadParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode parseJson(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new JobHandlerException("Invalid JSON payload: " + ex.getMessage());
        }
    }

    public String prettyJson(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception ex) {
            throw new JobHandlerException("Invalid JSON: " + ex.getMessage());
        }
    }

    public String prettyPrint(JsonNode node) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new JobHandlerException("Unable to format JSON node: " + ex.getMessage());
        }
    }

    public String requiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new JobHandlerException("Missing required field: " + field);
        }
        return value.asText();
    }

    public String optionalText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            return defaultValue;
        }
        return value.asText();
    }
}
