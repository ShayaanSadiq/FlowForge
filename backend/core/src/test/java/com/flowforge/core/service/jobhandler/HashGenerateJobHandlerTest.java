package com.flowforge.core.service.jobhandler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HashGenerateJobHandlerTest {

    private final HashGenerateJobHandler handler = new HashGenerateJobHandler(new JobPayloadParser());

    @Test
    void hashesPlainText() throws Exception {
        String result = handler.execute(
                jobWithPayload("hello"),
                msg -> {});

        assertThat(result).startsWith("SHA-256:");
        assertThat(result.length()).isGreaterThan(10);
    }

    @Test
    void hashesWithAlgorithmFromJson() throws Exception {
        String result = handler.execute(
                jobWithPayload("{\"text\":\"hello\",\"algorithm\":\"SHA-512\"}"),
                msg -> {});

        assertThat(result).startsWith("SHA-512:");
    }

    @Test
    void rejectsUnsupportedAlgorithm() {
        assertThatThrownBy(() -> handler.execute(
                jobWithPayload("{\"text\":\"hello\",\"algorithm\":\"MD5\"}"),
                msg -> {}))
                .isInstanceOf(JobHandlerException.class);
    }

    private static com.flowforge.core.domain.Job jobWithPayload(String payload) {
        com.flowforge.core.domain.Job job = new com.flowforge.core.domain.Job();
        job.setPayload(payload);
        return job;
    }
}
