package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.application;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.request.EligibleStudentRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.response.EligibleStudentImportResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.response.EligibleStudentImportResponse.RowError;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.response.EligibleStudentResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.persistence.entity.EligibleStudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.persistence.repository.EligibleStudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.UnauthorizedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.PageRequestFactory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin-only management of the pre-approved {@code eligible_students} roster. A student can only
 * register once an admin has added their index number and university email here — this is
 * ordinarily a once-per-academic-year bulk task at the start of the year, with one-by-one edits
 * afterwards for corrections.
 */
@Service
public class EligibleStudentService {

    private static final Pattern INDEX_NUMBER = Pattern.compile("^[A-Za-z]{2}/[0-9]{4}/[0-9]{5}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final EligibleStudentRepository repository;
    private final EligibleStudentFileParser fileParser;
    private final CurrentActorProvider currentActorProvider;

    public EligibleStudentService(
        EligibleStudentRepository repository,
        EligibleStudentFileParser fileParser,
        CurrentActorProvider currentActorProvider) {
        this.repository = repository;
        this.fileParser = fileParser;
        this.currentActorProvider = currentActorProvider;
    }

    @Transactional(readOnly = true)
    public PagedResponse<EligibleStudentResponse> list(String search, Integer page, Integer size, String sort) {
        requireAdmin();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        Page<EligibleStudentEntity> result = (search == null || search.isBlank())
            ? repository.findAll(pageable)
            : repository.search("%" + search.strip().toLowerCase(Locale.ROOT) + "%", pageable);
        return PagedResponse.of(result.map(this::toResponse), PageRequestFactory.describeSort(sort));
    }

    @Transactional
    public EligibleStudentResponse create(EligibleStudentRequest request) {
        requireAdmin();
        String indexNumber = normalizeIndexNumber(request.indexNumber());
        String email = normalizeEmail(request.universityEmail());
        if (repository.existsByIndexNumber(indexNumber)) {
            throw new ConflictException("An eligible student with this index number already exists.");
        }
        if (repository.existsByUniversityEmail(email)) {
            throw new ConflictException("An eligible student with this university email already exists.");
        }

        EligibleStudentEntity entity = new EligibleStudentEntity();
        entity.setId(UUID.randomUUID());
        entity.setIndexNumber(indexNumber);
        entity.setUniversityEmail(email);
        entity.setFullName(request.fullName().strip());
        entity.setAcademicLevel(request.academicLevel());
        entity.setActive(true);
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public EligibleStudentResponse update(UUID id, EligibleStudentRequest request) {
        requireAdmin();
        EligibleStudentEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Eligible student not found."));
        String indexNumber = normalizeIndexNumber(request.indexNumber());
        String email = normalizeEmail(request.universityEmail());
        if (repository.existsByIndexNumberAndIdNot(indexNumber, id)) {
            throw new ConflictException("An eligible student with this index number already exists.");
        }
        if (repository.existsByUniversityEmailAndIdNot(email, id)) {
            throw new ConflictException("An eligible student with this university email already exists.");
        }

        entity.setIndexNumber(indexNumber);
        entity.setUniversityEmail(email);
        entity.setFullName(request.fullName().strip());
        entity.setAcademicLevel(request.academicLevel());
        entity.setUpdatedAt(OffsetDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        requireAdmin();
        EligibleStudentEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Eligible student not found."));
        if (entity.getUserAccountId() != null) {
            throw new ConflictException(
                "This student has already registered and cannot be removed from the eligible roster.");
        }
        repository.delete(entity);
    }

    @Transactional
    public EligibleStudentImportResponse importFile(MultipartFile file) {
        requireAdmin();
        if (file == null || file.isEmpty()) {
            throw new ValidationException("An upload file is required.");
        }

        List<EligibleStudentFileParser.ParsedRow> rows = fileParser.parse(file);
        List<EligibleStudentEntity> toInsert = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        Set<String> seenIndexNumbers = new HashSet<>();
        Set<String> seenEmails = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int totalRows = 0;

        for (EligibleStudentFileParser.ParsedRow row : rows) {
            if (isBlank(row)) continue;
            totalRows++;
            try {
                String indexNumber = normalizeIndexNumber(requireField(row.indexNumber(), "Index number"));
                String email = normalizeEmail(requireField(row.universityEmail(), "University email"));
                String fullName = requireField(row.fullName(), "Full name");
                short academicLevel = parseAcademicLevel(row.academicLevel());

                if (!seenIndexNumbers.add(indexNumber)) {
                    throw new ValidationException("Duplicate index number within the uploaded file.");
                }
                if (!seenEmails.add(email)) {
                    throw new ValidationException("Duplicate university email within the uploaded file.");
                }
                if (repository.existsByIndexNumber(indexNumber)) {
                    throw new ValidationException("Index number already exists in the eligible roster.");
                }
                if (repository.existsByUniversityEmail(email)) {
                    throw new ValidationException("University email already exists in the eligible roster.");
                }

                EligibleStudentEntity entity = new EligibleStudentEntity();
                entity.setId(UUID.randomUUID());
                entity.setIndexNumber(indexNumber);
                entity.setUniversityEmail(email);
                entity.setFullName(fullName);
                entity.setAcademicLevel(academicLevel);
                entity.setActive(true);
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);
                toInsert.add(entity);
            } catch (ValidationException exception) {
                errors.add(new RowError(row.row(), exception.getMessage()));
            }
        }

        repository.saveAll(toInsert);
        return new EligibleStudentImportResponse(totalRows, toInsert.size(), errors.size(), errors);
    }

    private boolean isBlank(EligibleStudentFileParser.ParsedRow row) {
        return row.indexNumber().isBlank() && row.universityEmail().isBlank()
            && row.fullName().isBlank() && row.academicLevel().isBlank();
    }

    private String requireField(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(label + " is required.");
        }
        return value.strip();
    }

    private short parseAcademicLevel(String raw) {
        String value = requireField(raw, "Academic level");
        try {
            short level = Short.parseShort(value);
            if (level != 3 && level != 4) {
                throw new ValidationException("Academic level must be 3 or 4.");
            }
            return level;
        } catch (NumberFormatException exception) {
            throw new ValidationException("Academic level must be 3 or 4.");
        }
    }

    private String normalizeIndexNumber(String value) {
        String normalized = value.strip().toUpperCase(Locale.ROOT);
        if (!INDEX_NUMBER.matcher(normalized).matches()) {
            throw new ValidationException("Index number must look like CS/2022/00123.");
        }
        return normalized;
    }

    private String normalizeEmail(String value) {
        String normalized = value.strip().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new ValidationException("University email must be a valid email address.");
        }
        return normalized;
    }

    private void requireAdmin() {
        var actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot manage eligible students.");
        }
    }

    private EligibleStudentResponse toResponse(EligibleStudentEntity entity) {
        return new EligibleStudentResponse(
            entity.getId(),
            entity.getIndexNumber(),
            entity.getUniversityEmail(),
            entity.getFullName(),
            entity.getAcademicLevel(),
            entity.isActive(),
            entity.getUserAccountId() != null,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }
}
