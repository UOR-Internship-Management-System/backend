package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectStudentIdentityLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import org.springframework.stereotype.Component;

@Component
public class ProjectStudentIdentityLookupAdapter implements ProjectStudentIdentityLookup {

    private final StudentRepository studentRepository;

    public ProjectStudentIdentityLookupAdapter(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Optional<UUID> findStudentIdByUserAccountId(UUID userAccountId) {
        return studentRepository.findByUserAccountId(userAccountId).map(StudentEntity::getId);
    }
}
