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

    public static final String TEMPLATE_VERSION = "ATS-TEMPLATE-V2";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter YEAR = DateTimeFormatter.ofPattern("uuuu", Locale.ENGLISH);
    private static final String SEPARATOR = " \\textbullet{} ";

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
                \\usepackage[left=1.6cm,right=1.6cm,top=1.35cm,bottom=1.35cm]{geometry}
                \\usepackage{fontspec}
                \\usepackage{color}
                \\IfFontExistsTF{Liberation Sans}{\\setmainfont{Liberation Sans}}{}
                \\definecolor{cvink}{rgb}{0.10,0.11,0.13}
                \\definecolor{cvmuted}{rgb}{0.32,0.34,0.39}
                \\setlength{\\parindent}{0pt}
                \\setlength{\\parskip}{3.5pt}
                \\linespread{1.08}
                \\pagestyle{empty}
                \\makeatletter
                \\renewcommand{\\@listI}{\\leftmargin\\leftmargini\\itemsep 2pt\\parsep 0pt\\topsep 3pt\\partopsep 0pt}
                \\let\\@listi\\@listI
                \\makeatother
                \\newcommand{\\cvsection}[1]{\\vspace{12pt}{\\color{cvink}\\bfseries\\large\\MakeUppercase{#1}}\\par\\vspace{2.5pt}{\\color{cvink}\\hrule height 0.7pt}\\vspace{6pt}}
                \\newcommand{\\cvspacer}{\\vspace{8pt}}
                \\begin{document}
                %s
                \\end{document}
                """.formatted(body);
    }

    /**
     * Centered masthead: name, headline, then contact details grouped into aligned rows
     * (addresses and phone, then profile links, then location).
     */
    private void renderHeader(StringBuilder out, CvDocumentModel model) {
        String name = firstNonBlank(
                model.profile() == null ? null : model.profile().displayName(),
                model.identity().fullName());
        out.append("\\begin{center}\n");
        out.append("{\\Huge\\bfseries\\color{cvink} ").append(escape(name)).append("}\\par\n");
        if (model.profile() != null && hasText(model.profile().headline())) {
            out.append("\\vspace{4pt}{\\large\\color{cvmuted} ")
                    .append(escape(model.profile().headline()))
                    .append("}\\par\n");
        }
        out.append("\\vspace{7pt}\n");

        contactRow(out, joinEscaped(
                model.identity().universityEmail(),
                model.profile() == null ? null : model.profile().personalEmail(),
                model.profile() == null ? null : model.profile().phone()));

        contactRow(out, model.contactLinks().stream()
                .map(this::formatContactLink)
                .filter(this::hasText)
                .map(this::escape)
                .reduce((a, b) -> a + SEPARATOR + b)
                .orElse(""));

        String location = model.profile() == null ? null : model.profile().location();
        if (hasText(location)) contactRow(out, escape(location));

        out.append("\\end{center}\n");
        out.append("\\vspace{3pt}{\\color{cvink}\\hrule height 0.9pt}\n");
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
            entryBlock(out, item.degree(), item.institution(),
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
            entryBlock(out, item.positionTitle(), item.organization(),
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
            entryBlock(out, item.title(), null, formatRange(item.startDate(), item.endDate(), false), null);
            renderDescription(out, item.description());
            String skills = item.skills().stream()
                    .map(CvDocumentModel.ProjectSkill::skillName)
                    .filter(this::hasText)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            labeledLine(out, "Technologies", skills);
            appendLine(out, joinNonBlank(" \u2022 ", cleanUrl(item.repositoryUrl()), cleanUrl(item.demoUrl())));
        }
    }

    private void renderCertificates(StringBuilder out, List<CvDocumentModel.Certificate> items) {
        if (items.isEmpty()) return;
        section(out, "Certificates");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, item.title(), item.issuer(), formatDate(item.issueDate()), null);
            appendLine(out, cleanUrl(item.credentialUrl()));
        }
    }

    private void renderAwards(StringBuilder out, List<CvDocumentModel.Award> items) {
        if (items.isEmpty()) return;
        section(out, "Awards and Honors");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, item.title(), item.issuer(), formatDate(item.awardDate()), null);
            renderDescription(out, item.description());
        }
    }

    private void renderActivities(StringBuilder out, List<CvDocumentModel.Activity> items) {
        if (items.isEmpty()) return;
        section(out, "Extracurricular Activities");
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            spacer(out, i);
            entryBlock(out, item.activityName(), item.roleTitle(),
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

    /** Renders one centered header line of already-escaped contact text. */
    private void contactRow(StringBuilder out, String escapedContent) {
        if (!hasText(escapedContent)) return;
        out.append("\\vspace{2pt}{\\small\\color{cvmuted} ").append(escapedContent).append("}\\par\n");
    }

    /**
     * Renders a bold title with an optional italic subtitle (institution, employer, issuer) beside it
     * and the date right-aligned on the same line, followed by an optional italic meta line grouped
     * into the same paragraph so it stays visually tied to the title.
     */
    private void entryBlock(StringBuilder out, String title, String subtitle, String date, String meta) {
        if (!hasText(title) && !hasText(subtitle) && !hasText(date) && !hasText(meta)) return;
        out.append("\\noindent");
        if (hasText(title)) {
            out.append("{\\bfseries\\color{cvink} ").append(escape(title));
            if (hasText(subtitle)) out.append(",");
            out.append("}");
        }
        if (hasText(subtitle)) {
            if (hasText(title)) out.append(" ");
            out.append("{\\itshape ").append(escape(subtitle)).append("}");
        }
        if (hasText(date)) out.append("\\hfill{\\itshape\\color{cvmuted} ").append(escape(date)).append("}");
        if (hasText(meta)) out.append("\\\\{\\itshape\\color{cvmuted} ").append(escape(meta)).append("}");
        out.append("\\par\n");
    }

    /** Renders a plain-weight left value with an italic right-aligned value on the same line. */
    private void metaRow(StringBuilder out, String left, String right) {
        if (!hasText(left) && !hasText(right)) return;
        out.append("\\noindent{}");
        if (hasText(left)) out.append(escape(left));
        if (hasText(right)) out.append("\\hfill{\\itshape\\color{cvmuted} ").append(escape(right)).append("}");
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
        out.append("\\begin{itemize}\\setlength{\\itemsep}{2pt}\\setlength{\\parskip}{0pt}\\setlength{\\topsep}{3pt}\n");
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

    /** Builds one contact link as "Label: host/path", dropping the scheme and www noise. */
    private String formatContactLink(CvDocumentModel.ContactLink link) {
        String url = cleanUrl(link.url());
        if (!hasText(url)) return hasText(link.label()) ? link.label().strip() : "";
        if (!hasText(link.label())) return url;
        return link.label().strip() + ": " + url;
    }

    /** Strips scheme, www prefix, and trailing slashes so links stay readable on one header line. */
    private String cleanUrl(String url) {
        if (!hasText(url)) return "";
        String value = url.strip();
        String lower = value.toLowerCase(Locale.ENGLISH);
        if (lower.startsWith("https://")) value = value.substring(8);
        else if (lower.startsWith("http://")) value = value.substring(7);
        if (value.toLowerCase(Locale.ENGLISH).startsWith("www.")) value = value.substring(4);
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return value;
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
        return Arrays.stream(values).filter(this::hasText).reduce((a, b) -> a + delimiter + b).orElse("");
    }

    /** Escapes each value independently, then joins with the raw LaTeX header separator. */
    private String joinEscaped(String... values) {
        return Arrays.stream(values)
                .filter(this::hasText)
                .map(this::escape)
                .reduce((a, b) -> a + SEPARATOR + b)
                .orElse("");
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
