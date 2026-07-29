package com.flowforge.core.repository;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends MongoRepository<Job, String> {

    Page<Job> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Job> findTop10ByStatusOrderByCreatedAtAsc(JobStatus status);

    Optional<Job> findByIdAndUserId(String id, String userId);

    long countByStatus(JobStatus status);
}
