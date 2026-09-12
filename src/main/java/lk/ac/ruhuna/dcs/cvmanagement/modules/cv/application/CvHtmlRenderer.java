package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.StringJoiner;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.HtmlEscaper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.springframework.stereotype.Component;

/** Renders the backend-controlled ATS preview fragment from the canonical document model. */
@Component
public class CvHtmlRenderer {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM uuuu");
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("uuuu");

    public String render(CvDocumentModel model) {
        StringBuilder html = new StringBuilder(4096);
        html.append("<div class=\"cv-document\">");
        appendHeader(html, model);
        appendSummary(html, model);
        appendSkills(html, model);
        appendEducation(html, model);
        appendExperience(html, model);
        appendProjects(html, model);
        appendCertificates(html, model);
        appendAwards(html, model);
        appendActivities(html, model);
        appendAcademics(html, model);
        html.append("</div>");
        return html.toString();
    }

    private void appendHeader(StringBuilder html, CvDocumentModel model) {
        String displayName = model.profile() != null && hasText(model.profile().displayName())
                ? model.profile().displayName()
                : model.identity().fullName();
        html.append("<header class=\"cv-header\"><h1>").append(escape(displayName)).append("</h1>");
        if (model.profile() != null && hasText(model.profile().headline())) {
            html.append("<p class=\"cv-headline\">").append(escape(model.profile().headline())).append("</p>");
        }

        StringJoiner contact = new StringJoiner(" &bull; ");
        contact.add(escape(model.identity().universityEmail()));
        if (model.profile() != null && hasText(model.profile().personalEmail())
                && !model.profile().personalEmail().equalsIgnoreCase(model.identity().universityEmail())) {
            contact.add(escape(model.profile().personalEmail()));
        }
        if (model.profile() != null && hasText(model.profile().phone())) contact.add(escape(model.profile().phone()));
        if (contact.length() > 0) {
            html.append("<p class=\"cv-contact-row\">").append(contact).append("</p>");
        }

        if (!model.contactLinks().isEmpty()) {
            StringJoiner links = new StringJoiner(" &bull; ");
            for (var link : model.contactLinks()) {
                links.add(renderContactLink(link));
            }
            html.append("<p class=\"cv-contact-row\">").append(links).append("</p>");
        }

        String location = model.profile() == null ? null : model.profile().location();
        if (hasText(location)) {
            html.append("<p class=\"cv-contact-row\">").append(escape(location)).append("</p>");
        }
        html.append("</header>");
    }

    private String renderContactLink(CvDocumentModel.ContactLink link) {
        String href = safeWebUrl(link.url());
        if (href == null) {
            return escape(link.label());
        }
        String cleanedUrl = cleanUrl(link.url());
        String text = hasText(link.label()) && hasText(cleanedUrl)
                ? link.label().strip() + ": " + cleanedUrl
                : hasText(cleanedUrl) ? cleanedUrl : link.label();
        return "<a href=\"" + escape(href) + "\" rel=\"noopener noreferrer\">" + escape(text) + "</a>";
    }

    private void appendSummary(StringBuilder html, CvDocumentModel model) {
        if (model.profile() != null && hasText(model.profile().summary())) {
            sectionStart(html, "Professional Summary");
            html.append("<p>").append(escape(model.profile().summary())).append("</p></section>");
        }
    }

    private void appendSkills(StringBuilder html, CvDocumentModel model) {
        if (model.declaredSkills().isEmpty()) return;
        sectionStart(html, "Skills");
        StringJoiner skills = new StringJoiner(", ");
        model.declaredSkills().forEach(skill -> {
            if (!hasText(skill.skillName())) return;
            String entry = hasText(skill.competencyLevel())
                    ? escape(skill.skillName()) + " (" + escape(titleCase(skill.competencyLevel())) + ")"
                    : escape(skill.skillName());
            skills.add(entry);
        });
        html.append("<p>").append(skills).append("</p></section>");
    }

    private void appendEducation(StringBuilder html, CvDocumentModel model) {
        if (model.educationEntries().isEmpty()) return;
        sectionStart(html, "Education");
        model.educationEntries().forEach(item -> {
            html.append("<article>");
            boolean hasResultNote = hasText(item.resultNote());
            entryHead(html, item.degree(), item.institution(),
                    educationDateRange(item.startDate(), item.endDate(), item.current()),
                    hasResultNote ? null : item.location());
            if (hasResultNote) appendMeta(html, item.resultNote(), item.location());
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendExperience(StringBuilder html, CvDocumentModel model) {
        if (model.experiences().isEmpty()) return;
        sectionStart(html, "Work Experience");
        model.experiences().forEach(item -> {
            html.append("<article>");
            entryHead(html, item.positionTitle(), item.organization(),
                    dateRange(item.startDate(), item.endDate(), item.currentRole()), item.location());
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendProjects(StringBuilder html, CvDocumentModel model) {
        if (model.projects().isEmpty()) return;
        sectionStart(html, "Projects");
        model.projects().forEach(item -> {
            html.append("<article>");
            entryHead(html, item.title(), null, dateRange(item.startDate(), item.endDate(), false), null);
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            if (!item.skills().isEmpty()) {
                StringJoiner skills = new StringJoiner(", ");
                item.skills().forEach(skill -> skills.add(escape(skill.skillName())));
                html.append("<p><strong>Technologies:</strong> ").append(skills).append("</p>");
            }
            StringJoiner links = new StringJoiner(" &bull; ");
            appendLinkTo(links, "Repository", item.repositoryUrl());
            appendLinkTo(links, "Demo", item.demoUrl());
            if (links.length() > 0) html.append("<p class=\"cv-links-line\">").append(links).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendCertificates(StringBuilder html, CvDocumentModel model) {
        if (model.certificates().isEmpty()) return;
        sectionStart(html, "Certificates");
        model.certificates().forEach(item -> {
            html.append("<article>");
            entryHead(html, item.title(), item.issuer(), formatDate(item.issueDate()), null);
            StringJoiner links = new StringJoiner(" &bull; ");
            appendLinkTo(links, "Credential", item.credentialUrl());
            if (links.length() > 0) html.append("<p class=\"cv-links-line\">").append(links).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendAwards(StringBuilder html, CvDocumentModel model) {
        if (model.awards().isEmpty()) return;
        sectionStart(html, "Awards and Honors");
        model.awards().forEach(item -> {
            html.append("<article>");
            entryHead(html, item.title(), item.issuer(), formatDate(item.awardDate()), null);
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendActivities(StringBuilder html, CvDocumentModel model) {
        if (model.activities().isEmpty()) return;
        sectionStart(html, "Extracurricular Activities");
        model.activities().forEach(item -> {
            html.append("<article>");
            entryHead(html, item.activityName(), item.roleTitle(),
                    dateRange(item.startDate(), item.endDate(), item.endDate() == null && item.startDate() != null),
                    null);
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendAcademics(StringBuilder html, CvDocumentModel model) {
        if (model.academicSummary() == null) return;
        sectionStart(html, "Academic Summary");
        html.append("<p><strong>Computer Science GPA:</strong> ")
                .append(escape(model.academicSummary().computerScienceGpa().toPlainString()));
        if (model.academicSummary().totalCredits() != null) {
            html.append(" &bull; <strong>Completed Credits:</strong> ")
                    .append(escape(model.academicSummary().totalCredits().toPlainString()));
        }
        html.append("</p></section>");
    }

    private void sectionStart(StringBuilder html, String title) {
        html.append("<section><h2>").append(escape(title)).append("</h2>");
    }

    /**
     * Renders an entry's title row: bold title with an optional italic subtitle beside it and the
     * date right-aligned, followed by an optional italic meta line beneath (e.g. location).
     */
    private void entryHead(StringBuilder html, String title, String subtitle, String date, String meta) {
        if (!hasText(title) && !hasText(subtitle) && !hasText(date) && !hasText(meta)) return;
        html.append("<div class=\"cv-entry-row\"><p class=\"cv-entry-title\">");
        if (hasText(title)) html.append("<strong>").append(escape(title)).append("</strong>");
        if (hasText(subtitle)) html.append("<span class=\"cv-entry-org\">").append(escape(subtitle)).append("</span>");
        html.append("</p>");
        if (hasText(date)) html.append("<p class=\"cv-entry-date\">").append(escape(date)).append("</p>");
        html.append("</div>");
        if (hasText(meta)) html.append("<p class=\"cv-meta\">").append(escape(meta)).append("</p>");
    }

    private void appendMeta(StringBuilder html, String first, String second) {
        StringJoiner joiner = new StringJoiner(" &bull; ");
        if (hasText(first)) joiner.add(escape(first));
        if (hasText(second)) joiner.add(escape(second));
        String value = joiner.toString();
        if (!value.isEmpty()) html.append("<p class=\"cv-meta\">").append(value).append("</p>");
    }

    private void appendLinkTo(StringJoiner joiner, String label, String url) {
        String href = safeWebUrl(url);
        if (href == null) return;
        joiner.add("<a href=\"" + escape(href) + "\" rel=\"noopener noreferrer\">" + escape(label) + "</a>");
    }

    private String safeWebUrl(String value) {
        if (!hasText(value)) return null;
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null) return null;
            return (scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http")) ? uri.toASCIIString() : null;
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    /** Strips scheme, www prefix, and trailing slashes so links stay readable on one header line. */
    private String cleanUrl(String url) {
        if (!hasText(url)) return null;
        String value = url.strip();
        String lower = value.toLowerCase(Locale.ENGLISH);
        if (lower.startsWith("https://")) value = value.substring(8);
        else if (lower.startsWith("http://")) value = value.substring(7);
        if (value.toLowerCase(Locale.ENGLISH).startsWith("www.")) value = value.substring(4);
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return hasText(value) ? value : null;
    }

    private String titleCase(String value) {
        if (!hasText(value)) return "";
        String lower = value.toLowerCase(Locale.ENGLISH);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String educationDateRange(LocalDate start, LocalDate end, boolean current) {
        if (start == null && end == null && !current) return "";
        String from = start == null ? "" : YEAR_FORMAT.format(start);
        String to = current ? "Present" : (end == null ? "" : YEAR_FORMAT.format(end));
        if (from.isEmpty()) return to;
        if (to.isEmpty()) return from;
        return from + " – " + to;
    }

    private String dateRange(LocalDate start, LocalDate end, boolean current) {
        if (start == null && end == null && !current) return "";
        String from = formatDate(start);
        String to = current ? "Present" : formatDate(end);
        if (from.isEmpty()) return to;
        if (to.isEmpty()) return from;
        return from + " – " + to;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String escape(String value) {
        return HtmlEscaper.escape(value);
    }
}
