package com.flowforge.core.service.jobhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowforge.core.domain.Job;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

@Component
public class HttpRequestJobHandler {

    private static final int MAX_BODY_CHARS = 4000;

    private final JobPayloadParser payloadParser;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public HttpRequestJobHandler(JobPayloadParser payloadParser) {
        this.payloadParser = payloadParser;
    }

    public String execute(Job job, Consumer<String> log) throws Exception {
        JsonNode config = payloadParser.parseJson(job.getPayload());
        String url = payloadParser.requiredText(config, "url");
        String method = payloadParser.optionalText(config, "method", "GET").toUpperCase();

        validateUrl(url);
        log.accept("HTTP " + method + " " + url);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "FlowForge/1.0");

        if ("POST".equals(method)) {
            String body = payloadParser.optionalText(config, "body", "");
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
            if (config.has("contentType")) {
                requestBuilder.header("Content-Type", payloadParser.requiredText(config, "contentType"));
            } else if (!body.isBlank()) {
                requestBuilder.header("Content-Type", "application/json");
            }
        } else if ("HEAD".equals(method)) {
            requestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        } else if (!"GET".equals(method)) {
            throw new JobHandlerException("Supported methods: GET, POST, HEAD");
        }

        long start = System.currentTimeMillis();
        HttpResponse<String> response = httpClient.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());
        long durationMs = System.currentTimeMillis() - start;

        String body = response.body() == null ? "" : response.body();
        boolean truncated = body.length() > MAX_BODY_CHARS;
        String preview = truncated ? body.substring(0, MAX_BODY_CHARS) + "..." : body;

        log.accept("Status: " + response.statusCode());
        log.accept("Duration: " + durationMs + "ms");
        log.accept("Body length: " + body.length() + (truncated ? " (truncated in result)" : ""));

        return """
                status=%d
                durationMs=%d
                url=%s
                body=%s
                """.formatted(response.statusCode(), durationMs, url, preview).trim();
    }

    private void validateUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception ex) {
            throw new JobHandlerException("Invalid URL: " + url);
        }

        if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
            throw new JobHandlerException("Only http and https URLs are allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new JobHandlerException("URL must include a host");
        }

        String lowerHost = host.toLowerCase();
        if (lowerHost.equals("localhost") || lowerHost.equals("127.0.0.1") || lowerHost.startsWith("192.168.")
                || lowerHost.startsWith("10.") || lowerHost.endsWith(".local")) {
            throw new JobHandlerException("Requests to private/local hosts are not allowed");
        }
    }
}
