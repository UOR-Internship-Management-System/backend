package lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.api.dto.response.StudentDashboardMetricsResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentdashboard.persistence.StudentDashboardMetricsQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.DependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mirrors {@code AdminDashboardService}, scoped to the authenticated student. */
@Service
public class StudentDashboardService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final StudentDashboardMetricsQuery metricsQuery;
    private final Clock clock;

    public StudentDashboardService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        StudentDashboardMetricsQuery metricsQuery,
        Clock clock) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.metricsQuery = metricsQuery;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public StudentDashboardMetricsResponse getMetrics() {
        UUID studentId = currentStudentId();
        try {
            return new StudentDashboardMetricsResponse(
                metricsQuery.countProjects(studentId),
                metricsQuery.countShortlistMemberships(studentId),
                metricsQuery.countDeclaredSkills(studentId),
                metricsQuery.findOfficialGpa(studentId),
                Instant.now(clock));
        } catch (DataAccessException exception) {
            throw new DependencyUnavailableException(
                "Dashboard metrics cannot be loaded at this time.");
        }
    }

    private UUID currentStudentId() {
        CurrentActor actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        return studentRepository.findByUserAccountId(actor.userId())
            .orElseThrow(() -> new NotFoundException(
                "Student record not found for the authenticated account."))
            .getId();
    }
}
