package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Consumer;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.springframework.stereotype.Service;

/** Creates a deterministic SHA-256 fingerprint of every source value that can alter generated CV output. */
@Service
public class CvSourceFingerprintService {

    static final String FINGERPRINT_VERSION = "CV-SOURCE-V1";
    static final String TEMPLATE_VERSION = "ATS-TEMPLATE-V1";

    public String fingerprint(CvDocumentModel model) {
        MessageDigest digest = sha256();
        CanonicalWriter writer = new CanonicalWriter(value -> digest.update(value.getBytes(StandardCharsets.UTF_8)));

        writer.value(FINGERPRINT_VERSION);
        writer.value(TEMPLATE_VERSION);
        writeIdentity(writer, model);
        writeProfile(writer, model);
        model.contactLinks().forEach(link -> {
            writer.value(link.id()); writer.value(link.label()); writer.value(link.url()); writer.value(link.displayOrder());
            writer.value(link.version()); writer.value(link.updatedAt());
        });
        model.declaredSkills().forEach(skill -> {
            writer.value(skill.declaredSkillId()); writer.value(skill.skillId()); writer.value(skill.skillName());
            writer.value(skill.competencyLevel()); writer.value(skill.displayOrder()); writer.value(skill.version());
            writer.value(skill.updatedAt());
        });
        model.experiences().forEach(item -> {
            writer.value(item.id()); writer.value(item.organization()); writer.value(item.positionTitle()); writer.value(item.location());
            writer.value(item.startDate()); writer.value(item.endDate()); writer.value(item.currentRole()); writer.value(item.description());
            writer.value(item.version()); writer.value(item.updatedAt());
        });
        model.projects().forEach(item -> {
            writer.value(item.id()); writer.value(item.title()); writer.value(item.description()); writer.value(item.repositoryUrl());
            writer.value(item.demoUrl()); writer.value(item.startDate()); writer.value(item.endDate()); writer.value(item.version());
            writer.value(item.updatedAt());
            item.skills().forEach(skill -> {
                writer.value(skill.skillId()); writer.value(skill.skillName()); writer.value(skill.displayOrder());
            });
        });
        model.certificates().forEach(item -> {
            writer.value(item.id()); writer.value(item.title()); writer.value(item.issuer()); writer.value(item.issueDate());
            writer.value(item.credentialUrl()); writer.value(item.version()); writer.value(item.updatedAt());
        });
        model.awards().forEach(item -> {
            writer.value(item.id()); writer.value(item.title()); writer.value(item.issuer()); writer.value(item.awardDate());
            writer.value(item.description()); writer.value(item.version()); writer.value(item.updatedAt());
        });
        model.activities().forEach(item -> {
            writer.value(item.id()); writer.value(item.activityName()); writer.value(item.roleTitle()); writer.value(item.startDate());
            writer.value(item.endDate()); writer.value(item.description()); writer.value(item.version()); writer.value(item.updatedAt());
        });
        if (model.academicSummary() == null) {
            writer.value("NO_ACADEMIC_SUMMARY");
        } else {
            var academic = model.academicSummary();
            writer.value(academic.computerScienceGpa()); writer.value(academic.totalCredits());
            writer.value(academic.calculatedAt()); writer.value(academic.sourceUploadId());
        }
        writeIds(writer, model.configuration().includedExperienceIds());
        writeIds(writer, model.configuration().includedProjectIds());
        writeIds(writer, model.configuration().includedCertificateIds());
        writeIds(writer, model.configuration().includedAwardIds());
        writeIds(writer, model.configuration().includedActivityIds());

        return HexFormat.of().formatHex(digest.digest());
    }

    private void writeIdentity(CanonicalWriter writer, CvDocumentModel model) {
        var identity = model.identity();
        writer.value(identity.studentId()); writer.value(identity.fullName()); writer.value(identity.universityEmail());
        writer.value(identity.studentUpdatedAt());
    }

    private void writeProfile(CanonicalWriter writer, CvDocumentModel model) {
        var profile = model.profile();
        if (profile == null) {
            writer.value("NO_PROFILE");
            return;
        }
        writer.value(profile.profileId()); writer.value(profile.displayName()); writer.value(profile.personalEmail());
        writer.value(profile.headline()); writer.value(profile.summary()); writer.value(profile.phone()); writer.value(profile.location());
        writer.value(profile.version()); writer.value(profile.updatedAt());
    }

    private void writeIds(CanonicalWriter writer, Iterable<UUID> ids) {
        ids.forEach(writer::value);
        writer.value("END_IDS");
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable in this JVM.", exception);
        }
    }

    /** Length-prefixing prevents concatenation ambiguity without relying on JSON/map ordering. */
    private static final class CanonicalWriter {
        private final Consumer<String> sink;
        private CanonicalWriter(Consumer<String> sink) { this.sink = sink; }
        private void value(Object value) {
            String normalized = normalize(value);
            sink.accept(Integer.toString(normalized.length()));
            sink.accept(":");
            sink.accept(normalized);
            sink.accept(";");
        }

        private String normalize(Object value) {
            if (value == null) return "<null>";
            if (value instanceof OffsetDateTime timestamp) return timestamp.toInstant().toString();
            if (value instanceof BigDecimal decimal) return decimal.stripTrailingZeros().toPlainString();
            return value.toString();
        }
    }
}
