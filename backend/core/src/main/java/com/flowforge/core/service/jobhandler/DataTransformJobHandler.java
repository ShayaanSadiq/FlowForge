package com.flowforge.core.service.jobhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowforge.core.domain.Job;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DataTransformJobHandler {

    private static final Set<String> OPERATIONS = Set.of(
            "uppercase", "lowercase", "trim", "reverse", "slugify",
            "normalize_whitespace", "sort_lines", "dedupe_lines", "replace", "extract_field"
    );

    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");

    private final JobPayloadParser payloadParser;

    public DataTransformJobHandler(JobPayloadParser payloadParser) {
        this.payloadParser = payloadParser;
    }

    public String execute(Job job, Consumer<String> log) {
        TransformRequest request = parseRequest(job.getPayload());
        log.accept("Operation: " + request.operation());
        log.accept("Input length: " + request.text().length() + " chars");

        String result = switch (request.operation()) {
            case "uppercase" -> request.text().toUpperCase(Locale.ROOT);
            case "lowercase" -> request.text().toLowerCase(Locale.ROOT);
            case "trim" -> request.text().trim();
            case "reverse" -> new StringBuilder(request.text()).reverse().toString();
            case "slugify" -> slugify(request.text());
            case "normalize_whitespace" -> normalizeWhitespace(request.text());
            case "sort_lines" -> sortLines(request.text());
            case "dedupe_lines" -> dedupeLines(request.text());
            case "replace" -> replaceText(request);
            case "extract_field" -> extractField(request);
            default -> throw new JobHandlerException("Unsupported operation: " + request.operation());
        };

        log.accept("Output length: " + result.length() + " chars");
        return result;
    }

    private TransformRequest parseRequest(String payload) {
        String trimmed = payload.trim();
        if (!trimmed.startsWith("{")) {
            throw new JobHandlerException(
                    "DATA_TRANSFORM requires JSON, e.g. {\"operation\":\"uppercase\",\"text\":\"hello\"}");
        }

        JsonNode config = payloadParser.parseJson(trimmed);
        String operation = payloadParser.requiredText(config, "operation").toLowerCase(Locale.ROOT);
        if (!OPERATIONS.contains(operation)) {
            throw new JobHandlerException("Supported operations: " + String.join(", ", OPERATIONS));
        }

        String text = payloadParser.optionalText(config, "text", "");
        if (text.isBlank() && !"extract_field".equals(operation)) {
            throw new JobHandlerException("Missing required field: text");
        }

        String find = payloadParser.optionalText(config, "find", "");
        String replaceWith = payloadParser.optionalText(config, "replaceWith", "");
        String json = payloadParser.optionalText(config, "json", "");
        String field = payloadParser.optionalText(config, "field", "");

        if ("replace".equals(operation) && find.isEmpty()) {
            throw new JobHandlerException("replace requires non-empty find");
        }
        if ("extract_field".equals(operation) && (json.isBlank() || field.isBlank())) {
            throw new JobHandlerException("extract_field requires json and field");
        }

        return new TransformRequest(operation, text, find, replaceWith, json, field);
    }

    private String slugify(String text) {
        String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = NON_SLUG.matcher(normalized).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug;
    }

    private String normalizeWhitespace(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    private String sortLines(String text) {
        return Arrays.stream(text.split("\\R"))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private String dedupeLines(String text) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String line : text.split("\\R")) {
            unique.add(line);
        }
        return String.join("\n", unique);
    }

    private String replaceText(TransformRequest request) {
        return request.text().replace(request.find(), request.replaceWith());
    }

    private String extractField(TransformRequest request) {
        JsonNode root = payloadParser.parseJson(request.json());
        JsonNode current = root;
        for (String part : request.field().split("\\.")) {
            if (current == null || !current.has(part)) {
                throw new JobHandlerException("Field not found: " + request.field());
            }
            current = current.get(part);
        }
        if (current.isNull()) {
            return "";
        }
        if (current.isValueNode()) {
            return current.asText();
        }
        return payloadParser.prettyPrint(current);
    }

    private record TransformRequest(
            String operation,
            String text,
            String find,
            String replaceWith,
            String json,
            String field
    ) {}
}
