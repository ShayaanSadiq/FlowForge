package com.flowforge.core.service;

import com.flowforge.core.dto.CreateJobRequest;

import java.time.Instant;

import static com.flowforge.core.util.UserFacingMessages.SCHEDULE_BOTH_FIELDS;
import static com.flowforge.core.util.UserFacingMessages.SCHEDULE_NEGATIVE_DELAY;
import static com.flowforge.core.util.UserFacingMessages.SCHEDULE_PAST_TIME;
import static com.flowforge.core.util.UserFacingMessages.scheduleDelayTooLong;
import static com.flowforge.core.util.UserFacingMessages.scheduleTooFarAhead;

public final class JobScheduling {

    public static final int MAX_DELAY_SECONDS = 7 * 24 * 60 * 60;

    private JobScheduling() {
    }

    public static Instant resolveScheduledAt(CreateJobRequest request) {
        Instant now = Instant.now();
        boolean hasDelay = request.getDelaySeconds() != null;
        boolean hasScheduledAt = request.getScheduledAt() != null;

        if (hasDelay && hasScheduledAt) {
            throw new IllegalArgumentException(SCHEDULE_BOTH_FIELDS);
        }

        if (hasDelay) {
            int delaySeconds = request.getDelaySeconds();
            if (delaySeconds < 0) {
                throw new IllegalArgumentException(SCHEDULE_NEGATIVE_DELAY);
            }
            if (delaySeconds > MAX_DELAY_SECONDS) {
                throw new IllegalArgumentException(scheduleDelayTooLong(MAX_DELAY_SECONDS));
            }
            return now.plusSeconds(delaySeconds);
        }

        if (hasScheduledAt) {
            Instant scheduledAt = request.getScheduledAt();
            if (scheduledAt.isBefore(now.minusSeconds(5))) {
                throw new IllegalArgumentException(SCHEDULE_PAST_TIME);
            }
            if (scheduledAt.isAfter(now.plusSeconds(MAX_DELAY_SECONDS))) {
                throw new IllegalArgumentException(scheduleTooFarAhead(MAX_DELAY_SECONDS));
            }
            return scheduledAt;
        }

        return now;
    }

    public static boolean isScheduledForFuture(Instant scheduledAt) {
        return scheduledAt != null && scheduledAt.isAfter(Instant.now());
    }
}
