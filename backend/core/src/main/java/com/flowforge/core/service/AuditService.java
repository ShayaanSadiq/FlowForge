package com.flowforge.core.service;

import com.flowforge.core.domain.AuditEvent;
import com.flowforge.core.dto.AuditEventResponse;
import com.flowforge.core.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public void log(String userId, String action, String resourceType, String resourceId, String details) {
        AuditEvent event = AuditEvent.builder()
                .userId(userId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .build();
        auditEventRepository.save(event);
    }

    public List<AuditEventResponse> listJobEvents(String userId, String jobId) {
        return auditEventRepository
                .findByUserIdAndResourceTypeAndResourceIdOrderByCreatedAtDesc(userId, "JOB", jobId)
                .stream()
                .map(event -> AuditEventResponse.builder()
                        .id(event.getId())
                        .action(event.getAction())
                        .details(event.getDetails())
                        .createdAt(event.getCreatedAt())
                        .build())
                .toList();
    }
}
