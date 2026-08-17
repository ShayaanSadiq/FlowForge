package com.flowforge.core.util;

public final class UserFacingMessages {

    public static final String SCHEDULE_BOTH_FIELDS =
            "Choose either a delay or a specific run time, not both.";
    public static final String SCHEDULE_NEGATIVE_DELAY = "Delay must be zero or more seconds.";
    public static final String SCHEDULE_PAST_TIME = "Run time must be in the future.";
    public static final String EMAIL_TAKEN = "An account with this email already exists.";
    public static final String INVALID_CREDENTIALS = "Email or password is incorrect.";
    public static final String USER_NOT_FOUND = "User account was not found.";
    public static final String JOB_NOT_FOUND = "That job was not found.";
    public static final String RETRY_NOT_ALLOWED = "Only failed or dead-letter jobs can be retried.";

    private UserFacingMessages() {
    }

    public static String unsupportedStatusFilter(String filter) {
        return "Unknown status filter \"" + filter
                + "\". Try Pending, Running, Failed, Scheduled, or All.";
    }

    public static String deprecatedJobType(String type) {
        return "The job type \"" + type + "\" is no longer supported.";
    }

    public static String scheduleDelayTooLong(int maxDelaySeconds) {
        return "Jobs can only be delayed up to " + (maxDelaySeconds / 86_400) + " days.";
    }

    public static String scheduleTooFarAhead(int maxDelaySeconds) {
        return "Jobs can only be scheduled up to " + (maxDelaySeconds / 86_400) + " days ahead.";
    }
}
