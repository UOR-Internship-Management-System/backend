package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response.ProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectSkillLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectSkillSummary;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectStudentIdentityLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.mapper.ProjectMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.PageRequestFactory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final CurrentActorProvider currentActorProvider;
    private final ProjectStudentIdentityLookup studentIdentityLookup;
    private final ProjectRepository projectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final ProjectSkillLookup skillLookup;
    private final ProjectMapper mapper;

    public ProjectService(
        CurrentActorProvider currentActorProvider,
        ProjectStudentIdentityLookup studentIdentityLookup,
        ProjectRepository projectRepository,
        ProjectSkillRepository projectSkillRepository,
        ProjectSkillLookup skillLookup,
        ProjectMapper mapper) {
        this.currentActorProvider = currentActorProvider;
        this.studentIdentityLookup = studentIdentityLookup;
        this.projectRepository = projectRepository;
        this.projectSkillRepository = projectSkillRepository;
        this.skillLookup = skillLookup;
        this.mapper = mapper;
    }

    private UUID currentStudentId() {
        var actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        if (!actor.hasRole(RoleName.STUDENT)) {
            throw new ForbiddenException("A Student account is required to manage projects.");
        }
        return studentIdentityLookup.findStudentIdByUserAccountId(actor.userId())
            .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
    }

    private List<ProjectSkillSummary> loadSkills(UUID projectId) {
        List<UUID> skillIds = projectSkillRepository.findByIdProjectId(projectId).stream()
                .map(link -> link.getId().getSkillId())
                .toList();
        Map<UUID, ProjectSkillSummary> skills = skillLookup.findByIds(skillIds);
        return skillIds.stream()
                .map(skillId -> requireSkill(skills, skillId))
                .toList();
    }

    private LinkedHashSet<UUID> validateSkillIds(List<UUID> skillIds) {
        if (skillIds == null) {
            return null;
        }
        if (skillIds.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidationException("Project skill IDs cannot contain null values.");
        }
        LinkedHashSet<UUID> uniqueSkillIds = new LinkedHashSet<>(skillIds);
        if (uniqueSkillIds.size() != skillIds.size()) {
            throw new ValidationException("Project skill IDs must be unique.");
        }
        Map<UUID, ProjectSkillSummary> skills = skillLookup.findByIds(uniqueSkillIds);
        uniqueSkillIds.forEach(skillId -> requireSkill(skills, skillId));
        return uniqueSkillIds;
    }

    private void replaceSkillLinks(UUID projectId, LinkedHashSet<UUID> skillIds) {
        if (skillIds == null) {
            return;
        }
        projectSkillRepository.deleteByProjectId(projectId);
        for (UUID skillId : skillIds) {
            projectSkillRepository.save(new ProjectSkillEntity(projectId, skillId));
        }
    }

    private ProjectSkillSummary requireSkill(Map<UUID, ProjectSkillSummary> skills, UUID skillId) {
        ProjectSkillSummary skill = skills.get(skillId);
        if (skill == null) {
            throw new NotFoundException("Skill not found in taxonomy: " + skillId);
        }
        return skill;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> list(String search, Integer page, Integer size, String sort) {
        UUID studentId = currentStudentId();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";
        Page<ProjectEntity> result = projectRepository.search(studentId, searchPattern, pageable);
        Page<ProjectResponse> mapped = result.map(entity -> mapper.toResponse(entity, loadSkills(entity.getId())));
        return PagedResponse.of(mapped, PageRequestFactory.describeSort(sort));
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID projectId) {
        UUID studentId = currentStudentId();
        ProjectEntity entity = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found."));
        assertOwnership(entity.getStudentId(), studentId);
        return mapper.toResponse(entity, loadSkills(entity.getId()));
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        UUID studentId = currentStudentId();
        validateDateRange(request.startDate(), request.endDate());
        LinkedHashSet<UUID> skillIds = validateSkillIds(request.skillIds());
        ProjectEntity entity = new ProjectEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setRepositoryUrl(request.repositoryUrl());
        entity.setDemoUrl(request.demoUrl());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setIncludeInCv(request.includeInCv());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        ProjectEntity saved = projectRepository.save(entity);

        replaceSkillLinks(saved.getId(), skillIds);
        return mapper.toResponse(saved, loadSkills(saved.getId()));
    }

    @Transactional
    public ProjectResponse update(UUID projectId, ProjectUpdateRequest request, long ifMatchVersion) {
        UUID studentId = currentStudentId();
        ProjectEntity entity = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Project has been modified since it was last read.");
        }
        LinkedHashSet<UUID> skillIds = validateSkillIds(request.skillIds());

        if (request.title() != null) entity.setTitle(request.title());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.repositoryUrl() != null) entity.setRepositoryUrl(request.repositoryUrl());
        if (request.demoUrl() != null) entity.setDemoUrl(request.demoUrl());
        if (request.startDate() != null) entity.setStartDate(request.startDate());
        if (request.endDate() != null) entity.setEndDate(request.endDate());
        if (request.includeInCv() != null) entity.setIncludeInCv(request.includeInCv());
        validateDateRange(entity.getStartDate(), entity.getEndDate());
        entity.setUpdatedAt(OffsetDateTime.now());
        ProjectEntity saved = projectRepository.save(entity);

        if (skillIds != null) {
            replaceSkillLinks(saved.getId(), skillIds);
        }
        return mapper.toResponse(saved, loadSkills(saved.getId()));
    }

    @Transactional
    public void delete(UUID projectId, long ifMatchVersion) {
        UUID studentId = currentStudentId();
        ProjectEntity entity = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Project has been modified since it was last read.");
        }
        projectSkillRepository.deleteByProjectId(projectId);
        projectRepository.delete(entity);
    }

    private void assertOwnership(UUID resourceStudentId, UUID currentStudentId) {
        if (!resourceStudentId.equals(currentStudentId)) {
            throw new ForbiddenException("This project does not belong to the authenticated Student.");
        }
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ValidationException("End date cannot be before start date.");
        }
    }
}
