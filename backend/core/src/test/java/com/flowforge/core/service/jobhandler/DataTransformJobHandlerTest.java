package com.flowforge.core.service.jobhandler;

import com.flowforge.core.domain.Job;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataTransformJobHandlerTest {

    private final DataTransformJobHandler handler = new DataTransformJobHandler(new JobPayloadParser());

    @Test
    void uppercasesText() {
        String result = handler.execute(job("""
                {"operation":"uppercase","text":"hello flowforge"}
                """), msg -> {});

        assertThat(result).isEqualTo("HELLO FLOWFORGE");
    }

    @Test
    void slugifiesText() {
        String result = handler.execute(job("""
                {"operation":"slugify","text":"Hello FlowForge Jobs!"}
                """), msg -> {});

        assertThat(result).isEqualTo("hello-flowforge-jobs");
    }

    @Test
    void extractsNestedJsonField() {
        String result = handler.execute(job("""
                {"operation":"extract_field","json":"{\\"user\\":{\\"name\\":\\"Alice\\"}}","field":"user.name"}
                """), msg -> {});

        assertThat(result).isEqualTo("Alice");
    }

    @Test
    void rejectsPlainTextPayload() {
        assertThatThrownBy(() -> handler.execute(job("hello"), msg -> {}))
                .isInstanceOf(JobHandlerException.class);
    }

    private static Job job(String payload) {
        Job entity = new Job();
        entity.setPayload(payload);
        return entity;
    }
}
