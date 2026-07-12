package lk.ac.ruhuna.dcs.cvmanagement.modules.auth.application;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.jwt.JwtService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.request.AdminLoginRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.request.StudentLoginRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.response.AuthTokenResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.response.CurrentUserResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.UnauthorizedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.AccountType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String ACTIVE = "ACTIVE";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordHashService passwordHashService;
    private final JwtService jwtService;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public AuthService(
            JdbcTemplate jdbcTemplate,
            PasswordHashService passwordHashService,
            JwtService jwtService,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordHashService = passwordHashService;
        this.jwtService = jwtService;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional
    public AuthTokenResponse loginStudent(StudentLoginRequest request) {
        return login(normalizeEmail(request.universityEmail()), request.password(), RoleName.STUDENT);
    }

    @Transactional
    public AuthTokenResponse loginAdmin(AdminLoginRequest request) {
        return login(normalizeEmail(request.email()), request.password(), RoleName.ADMIN);
    }

    public CurrentUserResponse me() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        AccountRecord account = findAccountById(actor.userId())
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!ACTIVE.equals(account.status())) {
            throw new ForbiddenException("Account is not active.");
        }
        return toCurrentUser(account);
    }

    public void logout() {
        currentActorProvider.currentActor().ifPresent(actor -> auditEventPublisher.record(
                actor.userId(),
                actor.roles().stream().findFirst().map(Enum::name).orElse(null),
                "AUTH_LOGOUT",
                "user_account",
                actor.userId().toString()));
    }

    public Optional<AccountRecord> findResetEligible(AccountType accountType, String email) {
        Optional<AccountRecord> account = findAccountByEmail(normalizeEmail(email));
        if (account.isEmpty()
                || !ACTIVE.equals(account.get().status())
                || !account.get().roles().contains(accountType.role())) {
            return Optional.empty();
        }
        if (accountType == AccountType.ADMIN && !isActiveAdmin(account.get().id())) {
            return Optional.empty();
        }
        if (accountType == AccountType.STUDENT && !isLinkedStudent(account.get().id())) {
            return Optional.empty();
        }
        return account;
    }

    @Transactional
    public void updatePassword(UUID userAccountId, String newPassword) {
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                """
                UPDATE user_accounts
                SET password_hash = ?, account_status = 'ACTIVE',
                    password_changed_at = ?, updated_at = ?
                WHERE id = ?
                """,
                passwordHashService.hash(newPassword),
                Timestamp.from(now),
                Timestamp.from(now),
                userAccountId);
    }

    private AuthTokenResponse login(String email, String password, RoleName requiredRole) {
        Optional<AccountRecord> account = findAccountByEmail(email);
        if (account.isEmpty()
                || !ACTIVE.equals(account.get().status())
                || !account.get().roles().contains(requiredRole)
                || (requiredRole == RoleName.ADMIN && !isActiveAdmin(account.get().id()))
                || !passwordHashService.matches(password, account.get().passwordHash())) {
            auditEventPublisher.record(
                    account.map(AccountRecord::id).orElse(null),
                    requiredRole.name(),
                    "AUTH_LOGIN_FAILURE",
                    "user_account",
                    account.map(AccountRecord::id).map(UUID::toString).orElse(null));
            throw new UnauthorizedException("Invalid email or password.");
        }
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                "UPDATE user_accounts SET last_login_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(now),
                Timestamp.from(now),
                account.get().id());
        auditEventPublisher.record(
                account.get().id(),
                requiredRole.name(),
                requiredRole == RoleName.ADMIN ? "AUTH_ADMIN_LOGIN_SUCCESS" : "AUTH_STUDENT_LOGIN_SUCCESS",
                "user_account",
                account.get().id().toString());
        return tokenResponse(account.get());
    }

    private AuthTokenResponse tokenResponse(AccountRecord account) {
        List<String> roles = account.roles().stream().map(Enum::name).toList();
        String token = jwtService.issueAccessToken(account.id(), account.email(), roles);
        return new AuthTokenResponse(token, "Bearer", jwtService.accessTokenTtlSeconds(), toCurrentUser(account));
    }

    private CurrentUserResponse toCurrentUser(AccountRecord account) {
        Set<String> roles = new LinkedHashSet<>(account.roles().stream().map(Enum::name).toList());
        String primaryRole = roles.contains(RoleName.ADMIN.name()) ? RoleName.ADMIN.name() : RoleName.STUDENT.name();
        return new CurrentUserResponse(
                account.id(),
                account.id(),
                account.email(),
                account.displayName(),
                roles,
                primaryRole);
    }

    private Optional<AccountRecord> findAccountByEmail(String email) {
        try {
            AccountRecord account = jdbcTemplate.queryForObject(
                    """
                    SELECT ua.id, ua.university_email, ua.password_hash, ua.account_status,
                           COALESCE(au.display_name, es.full_name, ua.university_email) AS display_name
                    FROM user_accounts ua
                    LEFT JOIN admin_users au ON au.user_account_id = ua.id
                    LEFT JOIN eligible_students es ON es.user_account_id = ua.id
                    WHERE ua.university_email = ?
                    """,
                    this::mapAccount,
                    email);
            return Optional.of(account.withRoles(loadRoles(account.id())));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private Optional<AccountRecord> findAccountById(UUID userId) {
        try {
            AccountRecord account = jdbcTemplate.queryForObject(
                    """
                    SELECT ua.id, ua.university_email, ua.password_hash, ua.account_status,
                           COALESCE(au.display_name, es.full_name, ua.university_email) AS display_name
                    FROM user_accounts ua
                    LEFT JOIN admin_users au ON au.user_account_id = ua.id
                    LEFT JOIN eligible_students es ON es.user_account_id = ua.id
                    WHERE ua.id = ?
                    """,
                    this::mapAccount,
                    userId);
            return Optional.of(account.withRoles(loadRoles(account.id())));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private AccountRecord mapAccount(ResultSet rs, int rowNumber) throws SQLException {
        return new AccountRecord(
                rs.getObject("id", UUID.class),
                rs.getString("university_email"),
                rs.getString("password_hash"),
                rs.getString("account_status"),
                rs.getString("display_name"),
                Set.of());
    }

    private Set<RoleName> loadRoles(UUID userId) {
        return new LinkedHashSet<>(jdbcTemplate.query(
                """
                SELECT r.name
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = ?
                ORDER BY r.name
                """,
                (rs, rowNum) -> RoleName.fromAuthority(rs.getString("name")),
                userId));
    }

    private boolean isActiveAdmin(UUID userId) {
        Boolean result = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM admin_users WHERE user_account_id = ? AND is_active = TRUE)",
                Boolean.class,
                userId);
        return Boolean.TRUE.equals(result);
    }

    private boolean isLinkedStudent(UUID userId) {
        Boolean result = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM eligible_students WHERE user_account_id = ? AND is_active = TRUE)",
                Boolean.class,
                userId);
        return Boolean.TRUE.equals(result);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record AccountRecord(
            UUID id,
            String email,
            String passwordHash,
            String status,
            String displayName,
            Set<RoleName> roles) {

        AccountRecord withRoles(Set<RoleName> roles) {
            return new AccountRecord(id, email, passwordHash, status, displayName, Set.copyOf(roles));
        }
    }
}
