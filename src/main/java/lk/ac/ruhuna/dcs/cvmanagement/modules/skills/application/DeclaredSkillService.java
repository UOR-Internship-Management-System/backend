package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.application;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.request.DeclaredSkillCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.request.DeclaredSkillUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.DeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.mapper.SkillMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.DeclaredSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.DeclaredSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
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
public class DeclaredSkillService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final DeclaredSkillRepository declaredSkillRepository;
    private final SkillRepository skillRepository;
    private final SkillMapper mapper;

    public DeclaredSkillService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        DeclaredSkillRepository declaredSkillRepository,
        SkillRepository skillRepository,
        SkillMapper mapper) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.declaredSkillRepository = declaredSkillRepository;
        this.skillRepository = skillRepository;
        this.mapper = mapper;
    }

    private UUID currentStudentId() {
        var actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        return studentRepository.findByUserAccountId(actor.userId())
            .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."))
            .getId();
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeclaredSkillResponse> list(String search, Integer page, Integer size, String sort) {
        UUID studentId = currentStudentId();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";   // <-- add this line
        Page<DeclaredSkillEntity> result = declaredSkillRepository.search(studentId, searchPattern, pageable); // <-- change search -> searchPattern here
        Page<DeclaredSkillResponse> mapped = result.map(entity -> mapper.toResponse(entity, skillName(entity.getSkillId())));
        return PagedResponse.of(mapped, PageRequestFactory.describeSort(sort));
    }

    @Transactional
    public DeclaredSkillResponse create(DeclaredSkillCreateRequest request) {
        UUID studentId = currentStudentId();
        SkillEntity skill = skillRepository.findById(request.skillId())
            .orElseThrow(() -> new NotFoundException("Skill not found in taxonomy."));

        if (declaredSkillRepository.existsByStudentIdAndSkillId(studentId, skill.getId())) {
            throw new ConflictException("This skill is already declared.");
        }

        DeclaredSkillEntity entity = new DeclaredSkillEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setSkillId(skill.getId());
        entity.setCompetencyLevel(request.competencyLevel());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        DeclaredSkillEntity saved = declaredSkillRepository.save(entity);
        return mapper.toResponse(saved, skill.getSkillName());
    }

    @Transactional
    public DeclaredSkillResponse update(UUID declaredSkillId, DeclaredSkillUpdateRequest request, long ifMatchVersion) {
        UUID studentId = currentStudentId();
        DeclaredSkillEntity entity = declaredSkillRepository.findByIdAndStudentId(declaredSkillId, studentId)
            .orElseThrow(() -> new NotFoundException("Declared skill not found."));

        if (entity.getVersion() != ifMatchVersion) {
            throw new PreconditionFailedException("Declared skill has been modified since it was last read.");
        }

        entity.setCompetencyLevel(request.competencyLevel());
        entity.setUpdatedAt(OffsetDateTime.now());
        DeclaredSkillEntity saved = declaredSkillRepository.save(entity);
        return mapper.toResponse(saved, skillName(saved.getSkillId()));
    }

    @Transactional
    public void delete(UUID declaredSkillId, long ifMatchVersion) {
        UUID studentId = currentStudentId();
        DeclaredSkillEntity entity = declaredSkillRepository.findByIdAndStudentId(declaredSkillId, studentId)
            .orElseThrow(() -> new NotFoundException("Declared skill not found."));

        if (entity.getVersion() != ifMatchVersion) {
            throw new PreconditionFailedException("Declared skill has been modified since it was last read.");
        }
        declaredSkillRepository.delete(entity);
    }

    private String skillName(UUID skillId) {
        return skillRepository.findById(skillId).map(SkillEntity::getSkillName).orElse("Unknown skill");
    }
}
