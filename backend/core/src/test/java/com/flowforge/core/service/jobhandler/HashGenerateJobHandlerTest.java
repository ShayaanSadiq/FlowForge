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

    @Test
    void rejectsOversizedSingleInput() {
        assertThatThrownBy(() -> handler.execute(
                jobWithPayload("{\"text\":\"" + "a".repeat(100_001) + "\",\"algorithm\":\"SHA-256\"}"),
                msg -> {}))
                .isInstanceOf(JobHandlerException.class)
                .hasMessageContaining("100000");
    }

    @Test
    void rejectsTooManyLines() {
        assertThatThrownBy(() -> handler.execute(
                jobWithPayload("{\"text\":\"" + "line\\n".repeat(501) + "\",\"mode\":\"lines\"}"),
                msg -> {}))
                .isInstanceOf(JobHandlerException.class)
                .hasMessageContaining("500 lines");
    }

    private static com.flowforge.core.domain.Job jobWithPayload(String payload) {
        com.flowforge.core.domain.Job job = new com.flowforge.core.domain.Job();
        job.setPayload(payload);
        return job;
    }

    @Test
    void verifiesExpectedHash() throws Exception {
        String result = handler.execute(
                jobWithPayload("""
                        {"text":"hello","algorithm":"SHA-256","expected":"2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"}
                        """),
                msg -> {});

        assertThat(result).contains("verification=MATCH");
    }

    @Test
    void hashesEachLineInBatchMode() throws Exception {
        String result = handler.execute(
                jobWithPayload("""
                        {"text":"alpha\\nbeta","algorithm":"SHA-256","mode":"lines"}
                        """),
                msg -> {});

        assertThat(result).contains("alpha => SHA-256:");
        assertThat(result).contains("beta => SHA-256:");
    }
}
