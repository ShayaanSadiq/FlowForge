package com.flowforge.core.domain;

public enum JobType {
    PYTHON_SCRIPT,
    HTTP_REQUEST,
    JSON_FORMAT,
    CSV_ANALYZE,
    HASH_GENERATE,
    BASE64_CODEC,
    DATA_TRANSFORM,

    /** @deprecated Legacy demo job type kept for reading existing MongoDB documents. */
    @Deprecated
    SIMULATION,

    /** @deprecated Legacy demo job type kept for reading existing MongoDB documents. */
    @Deprecated
    REPORT_GENERATION;

    public boolean isDeprecated() {
        return switch (this) {
            case SIMULATION, REPORT_GENERATION -> true;
            default -> false;
        };
    }
}
