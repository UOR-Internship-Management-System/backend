package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.springframework.stereotype.Component;

/** Renders the canonical CV document model into a single-column ATS-oriented LaTeX document. */
@Component
public class LatexCvRenderer {

    public static final String TEMPLATE_VERSION = "ATS-TEMPLATE-V1";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH);

    public String render(CvDocumentModel model) {
        StringBuilder body = new StringBuilder(4096);
        renderHeader(body, model);
        renderSummary(body, model.profile());
        renderSkills(body, model.declaredSkills());
        renderExperiences(body, model.experiences());
        renderProjects(body, model.projects());
        renderCertificates(body, model.certificates());
        renderAwards(body, model.awards());
        renderActivities(body, model.activities());
        renderAcademics(body, model.academicSummary());

        return """
                \\documentclass[10pt,a4paper]{article}
                \\usepackage[margin=1.55cm]{geometry}
                \\usepackage{fontspec}
                \\setlength{\\parindent}{0pt}
                \\setlength{\\parskip}{3pt}
                \\pagestyle{empty}
                \\newcommand{\\cvsection}[1]{\\vspace{5pt}{\\large\\bfseries #1}\\par\\vspace{2pt}\\hrule\\vspace{4pt}}
                \\begin{document}
                %s
                \\end{document}
                """.formatted(body);
    }

    private void renderHeader(StringBuilder out, CvDocumentModel model) {
        String name = firstNonBlank(
                model.profile() == null ? null : model.profile().displayName(),
                model.identity().fullName());
        out.append("{\\LARGE\\bfseries ").append(escape(name)).append("}\\par\n");
        appendLine(out, joinNonBlank(" | ",
                model.identity().universityEmail(),
                model.profile() == null ? null : model.profile().personalEmail(),
                model.profile() == null ? null : model.profile().phone(),
                model.profile() == null ? null : model.profile().location()));
        for (CvDocumentModel.ContactLink link : model.contactLinks()) {
            appendLine(out, joinNonBlank(": ", link.label(), link.url()));
        }
    }

    private void renderSummary(StringBuilder out, CvDocumentModel.Profile profile) {
        if (profile == null) return;
        if (hasText(profile.headline()) || hasText(profile.summary())) {
            section(out, "Professional Summary");
            appendLine(out, profile.headline());
            appendLine(out, profile.summary());
        }
    }

    private void renderSkills(StringBuilder out, List<CvDocumentModel.DeclaredSkill> skills) {
        if (skills.isEmpty()) return;
        section(out, "Skills");
        appendLine(out, skills.stream().map(CvDocumentModel.DeclaredSkill::skillName).filter(this::hasText).toList(), ", ");
    }

    private void renderExperiences(StringBuilder out, List<CvDocumentModel.Experience> items) {
        if (items.isEmpty()) return;
        section(out, "Work Experience");
        for (var item : items) {
            boldLine(out, joinNonBlank(" — ", item.positionTitle(), item.organization()));
            appendLine(out, joinNonBlank(" | ", formatRange(item.startDate(), item.endDate(), item.currentRole()), item.location()));
            appendLine(out, item.description());
        }
    }

    private void renderProjects(StringBuilder out, List<CvDocumentModel.Project> items) {
        if (items.isEmpty()) return;
        section(out, "Projects");
        for (var item : items) {
            boldLine(out, item.title());
            appendLine(out, formatRange(item.startDate(), item.endDate(), false));
            appendLine(out, item.description());
            if (!item.skills().isEmpty()) {
                appendLine(out, "Skills: " + item.skills().stream().map(CvDocumentModel.ProjectSkill::skillName).filter(this::hasText).reduce((a,b) -> a + ", " + b).orElse(""));
            }
            appendLine(out, item.repositoryUrl());
            appendLine(out, item.demoUrl());
        }
    }

    private void renderCertificates(StringBuilder out, List<CvDocumentModel.Certificate> items) {
        if (items.isEmpty()) return;
        section(out, "Certificates");
        for (var item : items) {
            boldLine(out, joinNonBlank(" — ", item.title(), item.issuer()));
            appendLine(out, formatDate(item.issueDate()));
            appendLine(out, item.credentialUrl());
        }
    }

    private void renderAwards(StringBuilder out, List<CvDocumentModel.Award> items) {
        if (items.isEmpty()) return;
        section(out, "Awards and Honors");
        for (var item : items) {
            boldLine(out, joinNonBlank(" — ", item.title(), item.issuer()));
            appendLine(out, formatDate(item.awardDate()));
            appendLine(out, item.description());
        }
    }

    private void renderActivities(StringBuilder out, List<CvDocumentModel.Activity> items) {
        if (items.isEmpty()) return;
        section(out, "Extracurricular Activities");
        for (var item : items) {
            boldLine(out, joinNonBlank(" — ", item.activityName(), item.roleTitle()));
            appendLine(out, formatRange(item.startDate(), item.endDate(), item.endDate() == null && item.startDate() != null));
            appendLine(out, item.description());
        }
    }

    private void renderAcademics(StringBuilder out, CvDocumentModel.AcademicSummary summary) {
        if (summary == null) return;
        section(out, "Academic Summary");
        if (summary.computerScienceGpa() != null) appendLine(out, "Computer Science GPA: " + decimal(summary.computerScienceGpa()));
        if (summary.totalCredits() != null) appendLine(out, "Completed Credits: " + decimal(summary.totalCredits()));
    }

    private void section(StringBuilder out, String title) {
        out.append("\\cvsection{").append(escape(title)).append("}\n");
    }

    private void boldLine(StringBuilder out, String text) {
        if (hasText(text)) out.append("\\textbf{").append(escape(text)).append("}\\par\n");
    }

    private void appendLine(StringBuilder out, String text) {
        if (hasText(text)) out.append(escape(text)).append("\\par\n");
    }

    private void appendLine(StringBuilder out, List<String> values, String delimiter) {
        String joined = values.stream().filter(this::hasText).reduce((a,b) -> a + delimiter + b).orElse("");
        appendLine(out, joined);
    }

    private String formatRange(LocalDate start, LocalDate end, boolean current) {
        String left = formatDate(start);
        String right = current ? "Present" : formatDate(end);
        return joinNonBlank(" -- ", left, right);
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    private String decimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : (second == null ? "" : second);
    }

    private String joinNonBlank(String delimiter, String... values) {
        return java.util.Arrays.stream(values).filter(this::hasText).reduce((a,b) -> a + delimiter + b).orElse("");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** Escapes TeX metacharacters and strips control characters while preserving ordinary Unicode text. */
    public String escape(String value) {
        if (value == null) return "";
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isISOControl(ch) && ch != '\n' && ch != '\t') continue;
            switch (ch) {
                case '\\' -> out.append("\\textbackslash{}");
                case '{' -> out.append("\\{");
                case '}' -> out.append("\\}");
                case '$' -> out.append("\\$");
                case '&' -> out.append("\\&");
                case '#' -> out.append("\\#");
                case '%' -> out.append("\\%");
                case '_' -> out.append("\\_");
                case '~' -> out.append("\\textasciitilde{}");
                case '^' -> out.append("\\textasciicircum{}");
                case '\n', '\r', '\t' -> out.append(' ');
                default -> out.append(ch);
            }
        }
        return out.toString();
    }
}
