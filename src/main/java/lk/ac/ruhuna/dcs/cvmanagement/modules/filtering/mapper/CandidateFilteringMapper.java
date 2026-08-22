package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.mapper;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCriteriaResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringDeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRequestSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRunResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.model.CandidateFilteringCandidateCore;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateFilteringCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.GpaAvailabilityStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.CandidateFilterRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.CandidateMatchingSkillRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.FilterRequestSummaryRow;
import org.springframework.stereotype.Component;

/** Stateless mappings between Candidate Filtering API, application, and persistence models. */
@Component
public class CandidateFilteringMapper {

    public CandidateFilteringCriteria toCriteria(CandidateFilteringRunRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Candidate Filtering request is required.");
        }
        return new CandidateFilteringCriteria(
                request.requestId(),
                request.runtimeGpaLowerBound(),
                request.runtimeGpaUpperBound(),
                request.requestSkillIds(),
                request.additionalSkillIds(),
                request.skillMatchMode());
    }

    public CandidateFilteringRunResponse toRunResponse(
            FilterRunEntity run,
            FilterRequestSummaryRow requestSummary,
            CandidateFilteringCriteria criteria,
            long candidateCount) {
        return new CandidateFilteringRunResponse(
                run.getId(),
                toRequestSummaryResponse(requestSummary),
                toCriteriaResponse(criteria),
                candidateCount,
                run.getCreatedAt());
    }

    public CandidateFilteringCriteriaResponse toCriteriaResponse(CandidateFilteringCriteria criteria) {
        return new CandidateFilteringCriteriaResponse(
                criteria.requestId(),
                criteria.runtimeGpaLowerBound(),
                criteria.runtimeGpaUpperBound(),
                criteria.requestSkillIds(),
                criteria.additionalSkillIds(),
                criteria.skillMatchMode());
    }

    public CandidateFilteringRequestSummaryResponse toRequestSummaryResponse(FilterRequestSummaryRow row) {
        return new CandidateFilteringRequestSummaryResponse(
                row.requestId(),
                row.companyId(),
                row.companyName(),
                row.title(),
                row.shortlistGuidanceValue());
    }

    public CandidateFilteringCandidateCore toCandidateCore(
            CandidateFilterRow row,
            List<CandidateMatchingSkillRow> matchingSkills) {
        return new CandidateFilteringCandidateCore(
                row.studentId(),
                row.indexNumber(),
                row.fullName(),
                row.officialGpa(),
                row.officialGpa() == null ? GpaAvailabilityStatus.NOT_AVAILABLE : GpaAvailabilityStatus.AVAILABLE,
                matchingSkills == null
                        ? List.of()
                        : matchingSkills.stream().map(this::toDeclaredSkillResponse).toList(),
                row.declaredSkillCount());
    }

    private CandidateFilteringDeclaredSkillResponse toDeclaredSkillResponse(CandidateMatchingSkillRow row) {
        return new CandidateFilteringDeclaredSkillResponse(
                row.declaredSkillId(),
                row.skillId(),
                row.skillName(),
                row.competencyLevel(),
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }
}
