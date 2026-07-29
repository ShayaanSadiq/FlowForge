package com.flowforge.core.service;

import com.flowforge.core.domain.AuditEvent;
import com.flowforge.core.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
