package com.flowforge.core.service.jobhandler;

import com.flowforge.core.domain.Job;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class CsvAnalyzeJobHandler {

    public String execute(Job job, Consumer<String> log) {
        String csv = job.getPayload().trim();
        if (csv.isBlank()) {
            throw new JobHandlerException("CSV payload cannot be empty");
        }

        String[] lines = csv.split("\\R");
        List<String> nonEmptyLines = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                nonEmptyLines.add(line);
            }
        }

        if (nonEmptyLines.isEmpty()) {
            throw new JobHandlerException("CSV contains no data rows");
        }

        String[] headers = parseRow(nonEmptyLines.getFirst());
        int dataRows = nonEmptyLines.size() - 1;

        Map<String, Integer> columnFillCounts = new LinkedHashMap<>();
        for (String header : headers) {
            columnFillCounts.put(header, 0);
        }

        int emptyCells = 0;
        for (int i = 1; i < nonEmptyLines.size(); i++) {
            String[] values = parseRow(nonEmptyLines.get(i));
            for (int col = 0; col < headers.length; col++) {
                String value = col < values.length ? values[col].trim() : "";
                if (value.isEmpty()) {
                    emptyCells++;
                } else {
                    columnFillCounts.merge(headers[col], 1, Integer::sum);
                }
            }
        }

        log.accept("Rows (including header): " + nonEmptyLines.size());
        log.accept("Data rows: " + dataRows);
        log.accept("Columns: " + headers.length);

        StringBuilder result = new StringBuilder();
        result.append("rows=").append(nonEmptyLines.size()).append('\n');
        result.append("dataRows=").append(dataRows).append('\n');
        result.append("columns=").append(String.join(", ", headers)).append('\n');
        result.append("emptyCells=").append(emptyCells).append('\n');
        result.append("columnFillRates:\n");
        for (Map.Entry<String, Integer> entry : columnFillCounts.entrySet()) {
            double rate = dataRows == 0 ? 0 : (entry.getValue() * 100.0 / dataRows);
            result.append("  ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append('/').append(dataRows)
                    .append(String.format(" (%.1f%%)", rate)).append('\n');
        }

        return result.toString().trim();
    }

    private String[] parseRow(String line) {
        return line.split(",", -1);
    }
}
