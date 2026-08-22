package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.observability;

/** Stable low-cardinality application metric names. */
public final class MetricsNames {

    public static final String CANDIDATE_FILTERING_RUN_CREATED = "candidate_filtering.run.created";
    public static final String CANDIDATE_FILTERING_DATABASE_DURATION = "candidate_filtering.database.duration";
    public static final String CANDIDATE_FILTERING_DATABASE_FAILURES = "candidate_filtering.database.failures";
    public static final String CANDIDATE_FILTERING_CANDIDATE_COUNT = "candidate_filtering.candidate_count";

    private MetricsNames() {
    }
}
