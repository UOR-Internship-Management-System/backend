package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.mapper;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipCompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequestResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequiredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.CompanySnapshotProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.InternshipRequestDetailProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.InternshipRequiredSkillProjection;
import org.springframework.stereotype.Component;

/** Maps module-local persistence/read snapshots to the frozen public JSON shape. */
@Component
public class InternshipRequestMapper {

    public InternshipRequestResponse toResponse(
            InternshipRequestDetailProjection request,
            List<InternshipRequiredSkillProjection> skills) {
        return new InternshipRequestResponse(
                request.requestId(), toCompanyResponse(request.company()), request.title(), request.description(),
                request.shortlistGuidanceValue(), skills.stream().map(this::toSkillResponse).toList(),
                request.version(), request.createdAt(), request.updatedAt());
    }

    public InternshipCompanyResponse toCompanyResponse(CompanySnapshotProjection company) {
        return new InternshipCompanyResponse(
                company.companyId(), company.name(), company.websiteUrl(), company.contactPerson(),
                company.contactEmail(), company.contactPhone(), company.notes(), company.version(),
                company.createdAt(), company.updatedAt());
    }

    public InternshipRequiredSkillResponse toSkillResponse(InternshipRequiredSkillProjection skill) {
        return new InternshipRequiredSkillResponse(skill.requiredSkillId(), skill.skillId(), skill.skillName());
    }
}
