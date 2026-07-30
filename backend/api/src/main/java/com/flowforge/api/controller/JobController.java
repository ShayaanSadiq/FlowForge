package com.flowforge.api.controller;

import com.flowforge.core.domain.JobType;
import com.flowforge.core.dto.CreateJobRequest;
import com.flowforge.core.dto.JobResponse;
import com.flowforge.core.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(Authentication authentication, @Valid @RequestBody CreateJobRequest request) {
        return jobService.createJob(authentication.getName(), request);
    }

    @GetMapping
    public Page<JobResponse> listJobs(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) JobType type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return jobService.listJobs(authentication.getName(), status, type, pageable);
    }

    @GetMapping("/{jobId}")
    public JobResponse getJob(Authentication authentication, @PathVariable String jobId) {
        return jobService.getJob(authentication.getName(), jobId);
    }

    @PostMapping("/{jobId}/retry")
    public JobResponse retryJob(Authentication authentication, @PathVariable String jobId) {
        return jobService.retryJob(authentication.getName(), jobId);
    }
}
