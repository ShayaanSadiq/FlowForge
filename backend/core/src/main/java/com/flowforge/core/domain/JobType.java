package com.flowforge.core.domain;

public enum JobType {
    PYTHON_SCRIPT,
    JSON_FORMAT,
    CSV_ANALYZE,
    HASH_GENERATE,
    BASE64_CODEC,

    /** @deprecated Legacy job type kept for reading existing MongoDB documents. */
    @Deprecated
    HTTP_REQUEST,

    /** @deprecated Legacy job type kept for reading existing MongoDB documents. */
    @Deprecated
    DATA_TRANSFORM,

    /** @deprecated Legacy demo job type kept for reading existing MongoDB documents. */
    @Deprecated
    SIMULATION,

    /** @deprecated Legacy demo job type kept for reading existing MongoDB documents. */
    @Deprecated
    REPORT_GENERATION;

    public boolean isDeprecated() {
        return switch (this) {
            case HTTP_REQUEST, DATA_TRANSFORM, SIMULATION, REPORT_GENERATION -> true;
            default -> false;
        };
    }
}
