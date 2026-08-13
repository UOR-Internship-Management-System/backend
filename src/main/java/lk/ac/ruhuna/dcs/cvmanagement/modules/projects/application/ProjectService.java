package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response.ProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.mapper.ProjectMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.IndividualSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.mapper.SkillMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.PageRequestFactory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final SkillRepository skillRepository;
    private final ProjectMapper mapper;
    private final SkillMapper skillMapper;

    public ProjectService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        ProjectRepository projectRepository,
        ProjectSkillRepository projectSkillRepository,
        SkillRepository skillRepository,
        ProjectMapper mapper,
        SkillMapper skillMapper) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.projectRepository = projectRepository;
        this.projectSkillRepository = projectSkillRepository;
        this.skillRepository = skillRepository;
        this.mapper = mapper;
        this.skillMapper = skillMapper;
    }

    private UUID currentStudentId() {
        var actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        return studentRepository.findByUserAccountId(actor.userId())
            .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."))
            .getId();
    }

    private List<IndividualSkillResponse> loadSkills(UUID projectId) {
        return projectSkillRepository.findByIdProjectId(projectId).stream()
            .map(ps -> skillRepository.findById(ps.getId().getSkillId()).orElse(null))
            .filter(java.util.Objects::nonNull)
            .map(skillMapper::toResponse)
            .toList();
    }

    private void replaceSkillLinks(UUID projectId, List<UUID> skillIds) {
        projectSkillRepository.deleteByProjectId(projectId);
        if (skillIds == null) return;
        for (UUID skillId : skillIds) {
            SkillEntity skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found in taxonomy: " + skillId));
            projectSkillRepository.save(new ProjectSkillEntity(projectId, skill.getId()));
        }
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

        replaceSkillLinks(saved.getId(), request.skillIds());
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

        if (request.title() != null) entity.setTitle(request.title());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.repositoryUrl() != null) entity.setRepositoryUrl(request.repositoryUrl());
        if (request.demoUrl() != null) entity.setDemoUrl(request.demoUrl());
        if (request.startDate() != null) entity.setStartDate(request.startDate());
        if (request.endDate() != null) entity.setEndDate(request.endDate());
        if (request.includeInCv() != null) entity.setIncludeInCv(request.includeInCv());
        entity.setUpdatedAt(OffsetDateTime.now());
        ProjectEntity saved = projectRepository.save(entity);

        if (request.skillIds() != null) {
            replaceSkillLinks(saved.getId(), request.skillIds());
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
}
