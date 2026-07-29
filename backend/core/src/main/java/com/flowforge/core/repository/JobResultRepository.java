package com.flowforge.core.repository;

import com.flowforge.core.domain.JobResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JobResultRepository extends MongoRepository<JobResult, String> {

    Optional<JobResult> findByJobId(String jobId);
}
