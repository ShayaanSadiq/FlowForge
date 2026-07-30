package com.flowforge.core.service;

import com.flowforge.core.dto.CreateJobRequest;
import com.flowforge.core.domain.JobType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class JobSchedulingTest {

    @Test
    void defaultsToImmediateExecution() {
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.PYTHON_SCRIPT);
        request.setPayload("print('hi')");

        Instant scheduledAt = JobScheduling.resolveScheduledAt(request);

        assertThat(scheduledAt).isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
    }

    @Test
    void delaysBySeconds() {
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.PYTHON_SCRIPT);
        request.setPayload("print('hi')");
        request.setDelaySeconds(30);

        Instant scheduledAt = JobScheduling.resolveScheduledAt(request);

        assertThat(scheduledAt).isCloseTo(Instant.now().plusSeconds(30), within(2, ChronoUnit.SECONDS));
    }

    @Test
    void acceptsExplicitScheduledAt() {
        Instant future = Instant.now().plusSeconds(120);
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.PYTHON_SCRIPT);
        request.setPayload("print('hi')");
        request.setScheduledAt(future);

        assertThat(JobScheduling.resolveScheduledAt(request)).isEqualTo(future);
    }

    @Test
    void rejectsBothDelayAndScheduledAt() {
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.PYTHON_SCRIPT);
        request.setPayload("print('hi')");
        request.setDelaySeconds(10);
        request.setScheduledAt(Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> JobScheduling.resolveScheduledAt(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not both");
    }

    @Test
    void rejectsNegativeDelay() {
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.PYTHON_SCRIPT);
        request.setPayload("print('hi')");
        request.setDelaySeconds(-1);

        assertThatThrownBy(() -> JobScheduling.resolveScheduledAt(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsPastScheduledAt() {
        CreateJobRequest request = new CreateJobRequest();
        request.setType(JobType.PYTHON_SCRIPT);
        request.setPayload("print('hi')");
        request.setScheduledAt(Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> JobScheduling.resolveScheduledAt(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }
}
