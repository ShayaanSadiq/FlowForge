package com.flowforge.core.repository;

import com.flowforge.core.domain.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {

    java.util.List<AuditEvent> findByUserIdAndResourceTypeAndResourceIdOrderByCreatedAtDesc(
            String userId, String resourceType, String resourceId);
}
