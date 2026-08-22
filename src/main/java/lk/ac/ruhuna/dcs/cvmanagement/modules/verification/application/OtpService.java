package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.application;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email.OtpEmailSender;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpResendResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response.OtpVerifyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.domain.policy.OtpPurpose;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.domain.policy.OtpRateLimitPolicy;
import lk.ac.ruhuna.dcs.cvmanagement.modules.verification.domain.policy.OtpStatus;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventOutcome;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventSeverity;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.AccountType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final OtpEmailSender otpEmailSender;
    private final OtpRateLimitPolicy policy;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            OtpEmailSender otpEmailSender,
            OtpRateLimitPolicy policy,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.otpEmailSender = otpEmailSender;
        this.policy = policy;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional
    public OtpCreateResult createSignUpContext(UUID eligibleStudentId, String indexNumber, String email) {
        return createContext(OtpPurpose.SIGN_UP, AccountType.STUDENT, null, eligibleStudentId, indexNumber, email);
    }

    @Transactional
    public OtpCreateResult createResetContext(UUID userAccountId, AccountType accountType, String email) {
        return createContext(OtpPurpose.PASSWORD_RESET, accountType, userAccountId, null, "PASSWORD_RESET", email);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public OtpVerifyResponse verify(UUID contextId, OtpPurpose expectedPurpose, String otp) {
        OtpContext context = findRequired(contextId, expectedPurpose);
        Instant now = Instant.now(clock);
        ensurePendingForVerification(context, now);
        if (!passwordEncoder.matches(otp, context.otpHash())) {
            int attempts = context.attemptCount() + 1;
            OtpStatus status = attempts >= policy.maxAttempts() ? OtpStatus.BLOCKED : OtpStatus.PENDING;
            jdbcTemplate.update(
                    "UPDATE verification_sessions SET attempt_count = ?, status = ?, updated_at = ? WHERE id = ?",
                    attempts,
                    status.name(),
                    Timestamp.from(now),
                    context.id());
            AuditEventType eventType = status == OtpStatus.BLOCKED
                    ? AuditEventType.AUTH_OTP_MAX_ATTEMPTS_REACHED
                    : AuditEventType.AUTH_OTP_VERIFICATION_FAILED;
            auditEventPublisher.recordSecurityRequired(
                    context.userAccountId(),
                    context.accountType() == null ? null : context.accountType().name(),
                    eventType,
                    AuditEventOutcome.FAILED,
                    status == OtpStatus.BLOCKED ? AuditEventSeverity.HIGH : AuditEventSeverity.WARN,
                    "verification_session",
                    context.id().toString(),
                    Map.of("purpose", context.purpose().name()));
            throw new BadRequestException("The OTP is invalid or expired.");
        }
        jdbcTemplate.update(
                "UPDATE verification_sessions SET status = 'VERIFIED', verified_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(now),
                Timestamp.from(now),
                context.id());
        auditEventPublisher.recordSecurityRequired(
                context.userAccountId(),
                context.accountType() == null ? null : context.accountType().name(),
                AuditEventType.AUTH_OTP_VERIFIED,
                AuditEventOutcome.SUCCEEDED,
                AuditEventSeverity.INFO,
                "verification_session",
                context.id().toString(),
                Map.of("purpose", context.purpose().name()));
        return new OtpVerifyResponse(true);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public OtpResendResponse resend(UUID contextId, OtpPurpose expectedPurpose) {
        OtpContext context = findRequired(contextId, expectedPurpose);
        Instant now = Instant.now(clock);
        if (context.status() != OtpStatus.PENDING && context.status() != OtpStatus.EXPIRED) {
            throw new BadRequestException("This OTP context cannot be resent.");
        }
        Instant cooldownBase = context.lastResendAt() == null ? context.createdAt() : context.lastResendAt();
        Instant availableAt = cooldownBase.plus(policy.resendCooldown());
        if (now.isBefore(availableAt)) {
            throw new BadRequestException("Please wait before requesting another OTP.");
        }
        if (context.resendCount() >= policy.maxResends()) {
            jdbcTemplate.update(
                    "UPDATE verification_sessions SET status = 'BLOCKED', updated_at = ? WHERE id = ?",
                    Timestamp.from(now),
                    context.id());
            auditEventPublisher.recordSecurityRequired(
                    context.userAccountId(),
                    context.accountType() == null ? null : context.accountType().name(),
                    AuditEventType.AUTH_OTP_RESEND_LIMIT_REACHED,
                    AuditEventOutcome.DENIED,
                    AuditEventSeverity.HIGH,
                    "verification_session",
                    context.id().toString(),
                    Map.of("purpose", context.purpose().name()));
            throw new BadRequestException("OTP resend limit exceeded.");
        }
        String otp = generateOtp();
        Instant expiresAt = now.plus(policy.ttl());
        jdbcTemplate.update(
                """
                UPDATE verification_sessions
                SET otp_hash = ?, expires_at = ?, status = 'PENDING',
                    attempt_count = 0, resend_count = resend_count + 1,
                    last_resend_at = ?, updated_at = ?
                WHERE id = ?
                """,
                passwordEncoder.encode(otp),
                Timestamp.from(expiresAt),
                Timestamp.from(now),
                Timestamp.from(now),
                context.id());
        otpEmailSender.sendOtp(context.email(), expectedPurpose.name(), otp, expiresAt);
        auditEventPublisher.recordSecurityBestEffort(
                context.userAccountId(),
                context.accountType() == null ? null : context.accountType().name(),
                AuditEventType.AUTH_OTP_SENT,
                AuditEventOutcome.ATTEMPTED,
                AuditEventSeverity.INFO,
                "verification_session",
                context.id().toString(),
                Map.of("purpose", expectedPurpose.name(), "delivery", "RESEND"));
        return new OtpResendResponse("OTP resent successfully.", policy.ttl().toSeconds());
    }

    public OtpContext requireVerified(UUID contextId, OtpPurpose expectedPurpose) {
        OtpContext context = findRequired(contextId, expectedPurpose);
        if (context.status() != OtpStatus.VERIFIED) {
            throw new BadRequestException("OTP verification must be completed first.");
        }
        return context;
    }

    public OtpContext findRequired(UUID contextId, OtpPurpose expectedPurpose) {
        OtpContext context = find(contextId)
                .orElseThrow(() -> new NotFoundException("Verification context was not found."));
        if (context.purpose() != expectedPurpose) {
            throw new BadRequestException("Verification context is invalid.");
        }
        return context;
    }

    public void consume(UUID contextId) {
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                "UPDATE verification_sessions SET status = 'CONSUMED', consumed_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(now),
                Timestamp.from(now),
                contextId);
    }

    public long expiresInSeconds() {
        return policy.ttl().toSeconds();
    }

    private OtpCreateResult createContext(
            OtpPurpose purpose,
            AccountType accountType,
            UUID userAccountId,
            UUID eligibleStudentId,
            String indexNumber,
            String email) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(policy.ttl());
        String otp = generateOtp();
        jdbcTemplate.update(
                """
                INSERT INTO verification_sessions (
                    id, eligible_student_id, index_number, university_email, purpose, otp_hash,
                    expires_at, status, account_type, user_account_id, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, ?, ?, ?)
                """,
                id,
                eligibleStudentId,
                indexNumber,
                normalizeEmail(email),
                purpose.name(),
                passwordEncoder.encode(otp),
                Timestamp.from(expiresAt),
                accountType == null ? null : accountType.name(),
                userAccountId,
                Timestamp.from(now),
                Timestamp.from(now));
        otpEmailSender.sendOtp(email, purpose.name(), otp, expiresAt);
        auditEventPublisher.recordSecurityBestEffort(
                userAccountId,
                accountType == null ? null : accountType.name(),
                AuditEventType.AUTH_OTP_SENT,
                AuditEventOutcome.ATTEMPTED,
                AuditEventSeverity.INFO,
                "verification_session",
                id.toString(),
                Map.of("purpose", purpose.name(), "delivery", "INITIAL"));
        return new OtpCreateResult(id, expiresAt, policy.ttl());
    }

    private void ensurePendingForVerification(OtpContext context, Instant now) {
        if (context.status() != OtpStatus.PENDING) {
            throw new BadRequestException("The OTP context is not pending.");
        }
        if (context.attemptCount() >= policy.maxAttempts()) {
            jdbcTemplate.update(
                    "UPDATE verification_sessions SET status = 'BLOCKED', updated_at = ? WHERE id = ?",
                    Timestamp.from(now),
                    context.id());
            auditEventPublisher.recordSecurityRequired(
                    context.userAccountId(),
                    context.accountType() == null ? null : context.accountType().name(),
                    AuditEventType.AUTH_OTP_MAX_ATTEMPTS_REACHED,
                    AuditEventOutcome.DENIED,
                    AuditEventSeverity.HIGH,
                    "verification_session",
                    context.id().toString(),
                    Map.of("purpose", context.purpose().name()));
            throw new BadRequestException("OTP attempt limit exceeded.");
        }
        if (!now.isBefore(context.expiresAt())) {
            jdbcTemplate.update(
                    "UPDATE verification_sessions SET status = 'EXPIRED', updated_at = ? WHERE id = ?",
                    Timestamp.from(now),
                    context.id());
            throw new BadRequestException("The OTP is invalid or expired.");
        }
    }

    private Optional<OtpContext> find(UUID contextId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM verification_sessions WHERE id = ?",
                    this::mapContext,
                    contextId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private OtpContext mapContext(ResultSet rs, int rowNumber) throws SQLException {
        String accountType = rs.getString("account_type");
        return new OtpContext(
                rs.getObject("id", UUID.class),
                OtpPurpose.valueOf(rs.getString("purpose")),
                accountType == null ? null : AccountType.valueOf(accountType),
                rs.getObject("user_account_id", UUID.class),
                rs.getObject("eligible_student_id", UUID.class),
                rs.getString("university_email"),
                rs.getString("otp_hash"),
                OtpStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("expires_at").toInstant(),
                nullableInstant(rs.getTimestamp("verified_at")),
                nullableInstant(rs.getTimestamp("consumed_at")),
                rs.getInt("attempt_count"),
                rs.getInt("resend_count"),
                nullableInstant(rs.getTimestamp("last_resend_at")),
                rs.getTimestamp("created_at").toInstant());
    }

    private Instant nullableInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, policy.length());
        int floor = (int) Math.pow(10, policy.length() - 1);
        int value = secureRandom.nextInt(bound - floor) + floor;
        return String.valueOf(value);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record OtpCreateResult(UUID contextId, Instant expiresAt, Duration ttl) {
    }

    public record OtpContext(
            UUID id,
            OtpPurpose purpose,
            AccountType accountType,
            UUID userAccountId,
            UUID eligibleStudentId,
            String email,
            String otpHash,
            OtpStatus status,
            Instant expiresAt,
            Instant verifiedAt,
            Instant consumedAt,
            int attemptCount,
            int resendCount,
            Instant lastResendAt,
            Instant createdAt) {
    }
}
