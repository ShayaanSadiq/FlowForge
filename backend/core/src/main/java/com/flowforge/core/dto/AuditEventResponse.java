package com.flowforge.core.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditEventResponse {

    private String id;
    private String action;
    private String details;
    private Instant createdAt;
}
