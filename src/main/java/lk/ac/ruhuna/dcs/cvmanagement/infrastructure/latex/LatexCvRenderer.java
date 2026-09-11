package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.springframework.stereotype.Component;

/** Renders the canonical CV document model into a single-column ATS-oriented LaTeX document. */
@Component
public class LatexCvRenderer {

    public static final String TEMPLATE_VERSION = "ATS-TEMPLATE-V1";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter YEAR = DateTimeFormatter.ofPattern("uuuu", Locale.ENGLISH);

    public String render(CvDocumentModel model) {
        StringBuilder body = new StringBuilder(4096);
        renderHeader(body, model);
        renderSummary(body, model.profile());
        renderSkills(body, model.declaredSkills());
        renderEducation(body, model.educationEntries());
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
                \\usepackage{color}
                \\IfFontExistsTF{Liberation Sans}{\\setmainfont{Liberation Sans}}{}
                \\definecolor{cvaccent}{rgb}{0.11,0.22,0.37}
                \\setlength{\\parindent}{0pt}
                \\setlength{\\parskip}{4pt}
                \\linespread{1.05}
                \\pagestyle{empty}
                \\makeatletter
                \\renewcommand{\\@listI}{\\leftmargin\\leftmargini\\itemsep 1.5pt\\parsep 0pt\\topsep 2pt\\partopsep 0pt}
                \\let\\@listi\\@listI
                \\makeatother
                \\newcommand{\\cvsection}[1]{\\vspace{9pt}{\\color{cvaccent}\\bfseries\\large\\MakeUppercase{#1}}\\par\\vspace{1pt}{\\color{cvaccent}\\hrule height 0.8pt}\\vspace{5pt}}
                \\newcommand{\\cvspacer}{\\vspace{7pt}}
                \\begin{document}
                %s
                \\end{document}
                """.formatted(body);
    }

    private void renderHeader(StringBuilder out, CvDocumentModel model) {
        String name = firstNonBlank(
                model.profile() == null ? null : model.profile().displayName(),
                model.identity().fullName());
        out.append("\\begin{center}\n");
        out.append("{\\Huge\\bfseries ").append(escape(name)).append("}\\par\n");
        if (model.profile() != null && hasText(model.profile().headline())) {
            out.append("\\vspace{1pt}{\\large ").append(escape(model.profile().headline())).append("}\\par\n");
        }
        out.append("\\vspace{4pt}\n");
        String contactLine = joinNonBlank(" | ",
                model.identity().universityEmail(),
                model.profile() == null ? null : model.profile().personalEmail(),
                model.profile() == null ? null : model.profile().phone());
        String links = model.contactLinks().stream()
                .map(link -> joinNonBlank(": ", link.label(), link.url()))
                .filter(this::hasText)
                .reduce((a, b) -> a + " | " + b)
                .orElse("");
        String contactRow = joinNonBlank(" | ", contactLine, links);
        if (hasText(contactRow)) out.append("{\\small ").append(escape(contactRow)).append("}\\par\n");
        String location = model.profile() == null ? null : model.profile().location();
        if (hasText(location)) out.append("\\vspace{1pt}{\\small ").append(escape(location)).append("}\\par\n");
        out.append("\\end{center}\n");
        out.append("\\vspace{2pt}{\\color{cvaccent}\\hrule height 1pt}\\vspace{3pt}\n");
    }

    private void renderSummary(StringBuilder out, CvDocumentModel.Profile profile) {
        if (profile == null || !hasText(profile.summary())) return;
        section(out, "Professional Summary");
        appendLine(out, profile.summary());
    }

    private void renderSkills(StringBuilder out, List<CvDocumentModel.DeclaredSkill> skills) {
        if (skills.isEmpty()) return;
        section(out, "Skills");
        String joined = skills.stream()
                .filter(skill -> hasText(skill.skillName()))
                .map(skill -> hasText(skill.competencyLevel())
                        ? skill.skillName() + " (" + titleCase(skill.competencyLevel()) + ")"
                        : skill.skillName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        appendLine(out, joined);
    }

    private void renderEducation(StringBuilder out, List<CvDocumentModel.Education> items) {
        if (items.isEmpty()) return;
        section(out, "Education");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, joinNonBlank(", ", item.degree(), item.institution()),
                    formatYearRange(item.startDate(), item.endDate(), item.current()), null);
            metaRow(out, item.resultNote(), item.location());
        }
    }

    private void renderExperiences(StringBuilder out, List<CvDocumentModel.Experience> items) {
        if (items.isEmpty()) return;
        section(out, "Work Experience");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, joinNonBlank(" — ", item.positionTitle(), item.organization()),
                    formatRange(item.startDate(), item.endDate(), item.currentRole()), item.location());
            renderDescription(out, item.description());
        }
    }

    private void renderProjects(StringBuilder out, List<CvDocumentModel.Project> items) {
        if (items.isEmpty()) return;
        section(out, "Projects");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, item.title(), formatRange(item.startDate(), item.endDate(), false), null);
            renderDescription(out, item.description());
            String skills = item.skills().stream()
                    .map(CvDocumentModel.ProjectSkill::skillName)
                    .filter(this::hasText)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            labeledLine(out, "Technologies", skills);
            appendLine(out, joinNonBlank(" | ", item.repositoryUrl(), item.demoUrl()));
        }
    }

    private void renderCertificates(StringBuilder out, List<CvDocumentModel.Certificate> items) {
        if (items.isEmpty()) return;
        section(out, "Certificates");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, joinNonBlank(" — ", item.title(), item.issuer()), formatDate(item.issueDate()), null);
            appendLine(out, item.credentialUrl());
        }
    }

    private void renderAwards(StringBuilder out, List<CvDocumentModel.Award> items) {
        if (items.isEmpty()) return;
        section(out, "Awards and Honors");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, joinNonBlank(" — ", item.title(), item.issuer()), formatDate(item.awardDate()), null);
            renderDescription(out, item.description());
        }
    }

    private void renderActivities(StringBuilder out, List<CvDocumentModel.Activity> items) {
        if (items.isEmpty()) return;
        section(out, "Extracurricular Activities");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, joinNonBlank(" — ", item.activityName(), item.roleTitle()),
                    formatRange(item.startDate(), item.endDate(), item.endDate() == null && item.startDate() != null),
                    null);
            renderDescription(out, item.description());
        }
    }

    private void renderAcademics(StringBuilder out, CvDocumentModel.AcademicSummary summary) {
        if (summary == null) return;
        section(out, "Academic Summary");
        if (summary.computerScienceGpa() != null) {
            labeledLine(out, "Computer Science GPA", decimal(summary.computerScienceGpa()));
        }
        if (summary.totalCredits() != null) {
            labeledLine(out, "Completed Credits", decimal(summary.totalCredits()));
        }
    }

    private void section(StringBuilder out, String title) {
        out.append("\\cvsection{").append(escape(title)).append("}\n");
    }

    /**
     * Renders a bold title with the date right-aligned on the same line, followed by an optional
     * italic meta line (e.g. location) grouped into the same paragraph so it stays visually tied
     * to the title instead of drifting apart by a full paragraph gap.
     */
    private void entryBlock(StringBuilder out, String title, String date, String meta) {
        if (!hasText(title) && !hasText(date) && !hasText(meta)) return;
        out.append("\\noindent");
        if (hasText(title)) out.append("{\\bfseries ").append(escape(title)).append("}");
        if (hasText(date)) out.append("\\hfill{\\itshape ").append(escape(date)).append("}");
        if (hasText(meta)) out.append("\\\\{\\itshape ").append(escape(meta)).append("}");
        out.append("\\par\n");
    }

    /** Renders a plain-weight left value with an italic right-aligned value on the same line. */
    private void metaRow(StringBuilder out, String left, String right) {
        if (!hasText(left) && !hasText(right)) return;
        out.append("\\noindent{}");
        if (hasText(left)) out.append(escape(left));
        if (hasText(right)) out.append("\\hfill{\\itshape ").append(escape(right)).append("}");
        out.append("\\par\n");
    }

    /** Adds breathing room between repeated entries within a section (skipped before the first). */
    private void spacer(StringBuilder out, int index) {
        if (index > 0) out.append("\\cvspacer\n");
    }

    private void labeledLine(StringBuilder out, String label, String value) {
        if (!hasText(value)) return;
        out.append("\\textbf{").append(escape(label)).append(":} ").append(escape(value)).append("\\par\n");
    }

    private void appendLine(StringBuilder out, String text) {
        if (hasText(text)) out.append(escape(text)).append("\\par\n");
    }

    /** Splits free-form multi-line descriptions into a bulleted list; single-line text stays a plain paragraph. */
    private void renderDescription(StringBuilder out, String text) {
        if (!hasText(text)) return;
        List<String> lines = Arrays.stream(text.split("\\r?\\n"))
                .map(this::stripBulletPrefix)
                .filter(this::hasText)
                .toList();
        if (lines.isEmpty()) return;
        if (lines.size() == 1) {
            appendLine(out, lines.get(0));
            return;
        }
        out.append("\\begin{itemize}\\setlength{\\itemsep}{1pt}\\setlength{\\parskip}{0pt}\\setlength{\\topsep}{2pt}\n");
        for (String line : lines) {
            out.append("\\item ").append(escape(line)).append("\n");
        }
        out.append("\\end{itemize}\n");
    }

    private String stripBulletPrefix(String line) {
        String trimmed = line.strip();
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("\u2022 ")) {
            return trimmed.substring(2).strip();
        }
        if (trimmed.startsWith("\u2022")) return trimmed.substring(1).strip();
        return trimmed;
    }

    private String formatYearRange(LocalDate start, LocalDate end, boolean current) {
        String left = start == null ? "" : YEAR.format(start);
        String right = current ? "Present" : (end == null ? "" : YEAR.format(end));
        return joinNonBlank(" -- ", left, right);
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

    private String titleCase(String value) {
        if (!hasText(value)) return "";
        String lower = value.toLowerCase(Locale.ENGLISH);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
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
