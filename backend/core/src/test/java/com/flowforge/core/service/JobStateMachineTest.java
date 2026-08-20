package com.flowforge.core.service;

import com.flowforge.core.domain.Job;
import com.flowforge.core.domain.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobStateMachineTest {

    private JobStateMachine stateMachine;
    private Job job;

    @BeforeEach
    void setUp() {
        stateMachine = new JobStateMachine();
        job = Job.builder()
                .status(JobStatus.PENDING)
                .attempts(0)
                .maxAttempts(3)
                .build();
    }

    @Test
    void pendingToRunningToSucceeded() {
        stateMachine.markRunning(job);
        assertThat(job.getStatus()).isEqualTo(JobStatus.RUNNING);

        stateMachine.markSucceeded(job, "done");
        assertThat(job.getStatus()).isEqualTo(JobStatus.SUCCEEDED);
        assertThat(job.getResult()).isEqualTo("done");
    }

    @Test
    void failedJobRetriesUntilDeadLetter() {
        stateMachine.markRunning(job);

        JobStatus afterFirst = stateMachine.markFailed(job, "error 1");
        assertThat(afterFirst).isEqualTo(JobStatus.PENDING);
        assertThat(job.getAttempts()).isEqualTo(1);

        stateMachine.markRunning(job);
        JobStatus afterSecond = stateMachine.markFailed(job, "error 2");
        assertThat(afterSecond).isEqualTo(JobStatus.PENDING);

        stateMachine.markRunning(job);
        JobStatus afterThird = stateMachine.markFailed(job, "error 3");
        assertThat(afterThird).isEqualTo(JobStatus.DEAD_LETTER);
        assertThat(job.getStatus()).isEqualTo(JobStatus.DEAD_LETTER);
    }

    @Test
    void invalidTransitionThrows() {
        assertThatThrownBy(() -> stateMachine.markSucceeded(job, "done"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pendingJobCanBeCancelled() {
        stateMachine.markCancelled(job);

        assertThat(job.getStatus()).isEqualTo(JobStatus.CANCELLED);
        assertThat(job.getFinishedAt()).isNotNull();
    }

    @Test
    void runningJobCannotBeCancelled() {
        stateMachine.markRunning(job);

        assertThatThrownBy(() -> stateMachine.markCancelled(job))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending jobs can be cancelled");
    }
}
