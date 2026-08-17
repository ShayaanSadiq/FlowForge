package com.flowforge.core.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserFacingMessagesTest {

    @Test
    void formatsUnsupportedStatusFilter() {
        assertThat(UserFacingMessages.unsupportedStatusFilter("NOT_A_STATUS"))
                .contains("NOT_A_STATUS")
                .contains("Pending");
    }

    @Test
    void formatsDeprecatedJobType() {
        assertThat(UserFacingMessages.deprecatedJobType("HTTP_REQUEST"))
                .isEqualTo("The job type \"HTTP_REQUEST\" is no longer supported.");
    }

    @Test
    void formatsScheduleLimitsInDays() {
        assertThat(UserFacingMessages.scheduleDelayTooLong(604_800)).contains("7 days");
        assertThat(UserFacingMessages.scheduleTooFarAhead(604_800)).contains("7 days ahead");
    }
}
