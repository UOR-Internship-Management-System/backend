package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.InvalidFilterCriteriaException;

/**
 * Immutable, normalized runtime criteria for one deterministic Candidate Filtering run.
 *
 * <p>This policy validates only rules that can be decided without database access. Request existence,
 * request-skill ownership, and active taxonomy membership are validated by the application layer once
 * the filtering read repository is available.
 */
public record CandidateFilteringCriteria(
        UUID requestId,
        BigDecimal runtimeGpaLowerBound,
        BigDecimal runtimeGpaUpperBound,
        List<UUID> requestSkillIds,
        List<UUID> additionalSkillIds,
        FilterSkillMatchMode skillMatchMode) {

    public static final int MAX_SKILLS_PER_SOURCE = 100;
    private static final BigDecimal MIN_GPA = new BigDecimal("0.00");
    private static final BigDecimal MAX_GPA = new BigDecimal("4.00");

    public CandidateFilteringCriteria {
        if (requestId == null) {
            throw invalid("requestId is required.");
        }
        if (skillMatchMode == null) {
            throw invalid("skillMatchMode is required.");
        }

        validateGpa("runtimeGpaLowerBound", runtimeGpaLowerBound);
        validateGpa("runtimeGpaUpperBound", runtimeGpaUpperBound);
        if (runtimeGpaLowerBound != null
                && runtimeGpaUpperBound != null
                && runtimeGpaLowerBound.compareTo(runtimeGpaUpperBound) > 0) {
            throw invalid("Minimum GPA cannot exceed maximum GPA.");
        }

        requestSkillIds = immutableSkillIds("requestSkillIds", requestSkillIds);
        additionalSkillIds = immutableSkillIds("additionalSkillIds", additionalSkillIds);

        Set<UUID> overlap = new HashSet<>(requestSkillIds);
        overlap.retainAll(additionalSkillIds);
        if (!overlap.isEmpty()) {
            throw invalid("requestSkillIds and additionalSkillIds must not overlap.");
        }
    }

    public boolean hasGpaCriteria() {
        return runtimeGpaLowerBound != null || runtimeGpaUpperBound != null;
    }

    public boolean hasSkillCriteria() {
        return !requestSkillIds.isEmpty() || !additionalSkillIds.isEmpty();
    }

    /** Returns the selected skill IDs in deterministic source order without exposing mutable state. */
    public List<UUID> selectedSkillIds() {
        return Stream.concat(requestSkillIds.stream(), additionalSkillIds.stream()).toList();
    }

    public int selectedSkillCount() {
        return requestSkillIds.size() + additionalSkillIds.size();
    }

    private static void validateGpa(String field, BigDecimal value) {
        if (value == null) {
            return;
        }
        if (value.compareTo(MIN_GPA) < 0 || value.compareTo(MAX_GPA) > 0) {
            throw invalid(field + " must be between 0.00 and 4.00.");
        }
        if (value.scale() > 2) {
            throw invalid(field + " may use at most two decimal places.");
        }
    }

    private static List<UUID> immutableSkillIds(String field, List<UUID> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        if (values.size() > MAX_SKILLS_PER_SOURCE) {
            throw invalid(field + " may contain at most 100 skills.");
        }
        if (values.stream().anyMatch(java.util.Objects::isNull)) {
            throw invalid(field + " must not contain null skill IDs.");
        }
        if (new HashSet<>(values).size() != values.size()) {
            throw invalid(field + " must not contain duplicate skill IDs.");
        }
        return List.copyOf(values);
    }

    private static InvalidFilterCriteriaException invalid(String message) {
        return new InvalidFilterCriteriaException(message);
    }
}
