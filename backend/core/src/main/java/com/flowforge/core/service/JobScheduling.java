package com.flowforge.core.service;

import com.flowforge.core.dto.CreateJobRequest;

import java.time.Instant;

public final class JobScheduling {

    public static final int MAX_DELAY_SECONDS = 7 * 24 * 60 * 60;

    private JobScheduling() {
    }

    public static Instant resolveScheduledAt(CreateJobRequest request) {
        Instant now = Instant.now();
        boolean hasDelay = request.getDelaySeconds() != null;
        boolean hasScheduledAt = request.getScheduledAt() != null;

        if (hasDelay && hasScheduledAt) {
            throw new IllegalArgumentException("Provide either delaySeconds or scheduledAt, not both");
        }

        if (hasDelay) {
            int delaySeconds = request.getDelaySeconds();
            if (delaySeconds < 0) {
                throw new IllegalArgumentException("delaySeconds must be zero or positive");
            }
            if (delaySeconds > MAX_DELAY_SECONDS) {
                throw new IllegalArgumentException("delaySeconds cannot exceed " + MAX_DELAY_SECONDS);
            }
            return now.plusSeconds(delaySeconds);
        }

        if (hasScheduledAt) {
            Instant scheduledAt = request.getScheduledAt();
            if (scheduledAt.isBefore(now.minusSeconds(5))) {
                throw new IllegalArgumentException("scheduledAt must be in the future");
            }
            if (scheduledAt.isAfter(now.plusSeconds(MAX_DELAY_SECONDS))) {
                throw new IllegalArgumentException("scheduledAt cannot be more than 7 days ahead");
            }
            return scheduledAt;
        }

        return now;
    }

    public static boolean isScheduledForFuture(Instant scheduledAt) {
        return scheduledAt != null && scheduledAt.isAfter(Instant.now());
    }
}
