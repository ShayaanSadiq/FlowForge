package com.flowforge.core.repository;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import com.flowforge.core.domain.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends MongoRepository<Job, String> {

    Page<Job> findByUserId(String userId, Pageable pageable);

    Page<Job> findByUserIdAndStatus(String userId, JobStatus status, Pageable pageable);

    Page<Job> findByUserIdAndType(String userId, JobType type, Pageable pageable);

    Page<Job> findByUserIdAndStatusAndType(String userId, JobStatus status, JobType type, Pageable pageable);

    @Query("""
            {
              'userId': ?0,
              'status': 'PENDING',
              'scheduledAt': { $gt: ?1 }
            }
            """)
    Page<Job> findScheduledByUserId(String userId, Instant now, Pageable pageable);

    @Query("""
            {
              'userId': ?0,
              'status': 'PENDING',
              'scheduledAt': { $gt: ?1 },
              'type': ?2
            }
            """)
    Page<Job> findScheduledByUserIdAndType(String userId, Instant now, JobType type, Pageable pageable);

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
