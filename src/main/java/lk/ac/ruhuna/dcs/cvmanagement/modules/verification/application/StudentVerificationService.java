package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.application;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.PasswordCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.request.StudentVerificationStartRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpResendResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpVerifyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.StudentVerificationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.domain.policy.OtpPurpose;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentVerificationService {

    private final JdbcTemplate jdbcTemplate;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public StudentVerificationService(
            JdbcTemplate jdbcTemplate,
            OtpService otpService,
            PasswordEncoder passwordEncoder,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional
    public StudentVerificationResponse start(StudentVerificationStartRequest request) {
        String indexNumber = request.indexNumber().trim().toUpperCase(Locale.ROOT);
        String email = normalizeEmail(request.universityEmail());
        EligibleStudent student = findEligibleStudent(indexNumber, email)
                .orElseThrow(() -> new NotFoundException("No eligible student record matches the submitted details."));
        if (student.userAccountId() != null && isActiveAccount(student.userAccountId())) {
            throw new ConflictException("This student account has already been activated.");
        }
        OtpService.OtpCreateResult result = otpService.createSignUpContext(student.id(), indexNumber, email);
        auditEventPublisher.record(
                student.userAccountId(),
                RoleName.STUDENT.name(),
                "AUTH_STUDENT_VERIFICATION_STARTED",
                "eligible_student",
                student.id().toString());
        return new StudentVerificationResponse(
                result.contextId(),
                "OTP_SENT",
                "Verification OTP sent to the university email.",
                result.expiresAt());
    }

    public OtpVerifyResponse verifyOtp(UUID verificationId, String otp) {
        return otpService.verify(verificationId, OtpPurpose.SIGN_UP, otp);
    }

    public OtpResendResponse resendOtp(UUID verificationId) {
        return otpService.resend(verificationId, OtpPurpose.SIGN_UP);
    }

    @Transactional
    public void createPassword(UUID verificationId, PasswordCreateRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Password confirmation does not match.");
        }
        OtpService.OtpContext context = otpService.requireVerified(verificationId, OtpPurpose.SIGN_UP);
        if (context.eligibleStudentId() == null) {
            throw new BadRequestException("Verification context is invalid.");
        }
        EligibleStudent student = findEligibleStudentById(context.eligibleStudentId())
                .orElseThrow(() -> new NotFoundException("Eligible student record was not found."));
        UUID userAccountId = student.userAccountId();
        Instant now = Instant.now(clock);
        if (userAccountId == null) {
            userAccountId = UUID.randomUUID();
            jdbcTemplate.update(
                    """
                    INSERT INTO user_accounts (
                        id, university_email, password_hash, account_status,
                        password_changed_at, created_at, updated_at
                    )
                    VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?)
                    """,
                    userAccountId,
                    student.email(),
                    passwordEncoder.encode(request.newPassword()),
                    Timestamp.from(now),
                    Timestamp.from(now),
                    Timestamp.from(now));
            assignRole(userAccountId, RoleName.STUDENT);
            jdbcTemplate.update(
                    "UPDATE eligible_students SET user_account_id = ?, updated_at = ? WHERE id = ?",
                    userAccountId,
                    Timestamp.from(now),
                    student.id());
        } else {
            jdbcTemplate.update(
                    """
                    UPDATE user_accounts
                    SET password_hash = ?, account_status = 'ACTIVE',
                        password_changed_at = ?, updated_at = ?
                    WHERE id = ?
                    """,
                    passwordEncoder.encode(request.newPassword()),
                    Timestamp.from(now),
                    Timestamp.from(now),
                    userAccountId);
            assignRole(userAccountId, RoleName.STUDENT);
        }
        otpService.consume(context.id());
        auditEventPublisher.record(
                userAccountId,
                RoleName.STUDENT.name(),
                "AUTH_STUDENT_PASSWORD_CREATED",
                "user_account",
                userAccountId.toString());
    }

    private Optional<EligibleStudent> findEligibleStudent(String indexNumber, String email) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    """
                    SELECT id, index_number, university_email, full_name, user_account_id
                    FROM eligible_students
                    WHERE index_number = ? AND university_email = ?
                      AND academic_level IN (3, 4) AND is_active = TRUE
                    """,
                    this::mapEligibleStudent,
                    indexNumber,
                    email));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private Optional<EligibleStudent> findEligibleStudentById(UUID id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    """
                    SELECT id, index_number, university_email, full_name, user_account_id
                    FROM eligible_students
                    WHERE id = ? AND academic_level IN (3, 4) AND is_active = TRUE
                    """,
                    this::mapEligibleStudent,
                    id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private EligibleStudent mapEligibleStudent(ResultSet rs, int rowNumber) throws SQLException {
        return new EligibleStudent(
                rs.getObject("id", UUID.class),
                rs.getString("index_number"),
                rs.getString("university_email"),
                rs.getString("full_name"),
                rs.getObject("user_account_id", UUID.class));
    }

    private boolean isActiveAccount(UUID userAccountId) {
        Boolean active = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM user_accounts WHERE id = ? AND account_status = 'ACTIVE')",
                Boolean.class,
                userAccountId);
        return Boolean.TRUE.equals(active);
    }

    private void assignRole(UUID userAccountId, RoleName role) {
        Integer existing = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = ? AND r.name = ?
                """,
                Integer.class,
                userAccountId,
                role.authority());
        if (existing != null && existing > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) SELECT ?, id FROM roles WHERE name = ?",
                userAccountId,
                role.authority());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record EligibleStudent(
            UUID id,
            String indexNumber,
            String email,
            String fullName,
            UUID userAccountId) {
    }
}
