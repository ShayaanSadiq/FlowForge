package com.flowforge.core.repository;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends MongoRepository<Job, String> {

    Page<Job> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("""
            {
              'status': ?0,
              $or: [
                { 'scheduledAt': null },
                { 'scheduledAt': { $exists: false } },
                { 'scheduledAt': { $lte: ?1 } }
              ]
            }
            """)
    List<Job> findTop10ReadyJobsOrderByScheduledAtAscCreatedAtAsc(JobStatus status, Instant now);

    long countByStatusAndScheduledAtAfter(JobStatus status, Instant now);

    Optional<Job> findByIdAndUserId(String id, String userId);

    long countByStatus(JobStatus status);
}
