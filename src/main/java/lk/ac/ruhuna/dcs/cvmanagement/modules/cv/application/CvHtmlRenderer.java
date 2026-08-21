package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.HtmlEscaper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import org.springframework.stereotype.Component;

/** Renders the backend-controlled ATS preview fragment from the canonical document model. */
@Component
public class CvHtmlRenderer {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM uuuu");

    public String render(CvDocumentModel model) {
        StringBuilder html = new StringBuilder(4096);
        html.append("<div class=\"cv-document\">");
        appendHeader(html, model);
        appendSummary(html, model);
        appendSkills(html, model);
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
        StringJoiner contact = new StringJoiner(" &middot; ");
        contact.add(escape(model.identity().universityEmail()));
        if (model.profile() != null && hasText(model.profile().personalEmail())
                && !model.profile().personalEmail().equalsIgnoreCase(model.identity().universityEmail())) {
            contact.add(escape(model.profile().personalEmail()));
        }
        if (model.profile() != null && hasText(model.profile().phone())) contact.add(escape(model.profile().phone()));
        if (model.profile() != null && hasText(model.profile().location())) contact.add(escape(model.profile().location()));
        html.append("<p class=\"cv-contact\">").append(contact).append("</p>");
        if (!model.contactLinks().isEmpty()) {
            html.append("<p class=\"cv-links\">");
            for (int i = 0; i < model.contactLinks().size(); i++) {
                var link = model.contactLinks().get(i);
                if (i > 0) html.append(" &middot; ");
                String href = safeWebUrl(link.url());
                if (href == null) {
                    html.append(escape(link.label()));
                } else {
                    html.append("<a href=\"").append(escape(href)).append("\" rel=\"noopener noreferrer\">")
                            .append(escape(link.label())).append("</a>");
                }
            }
            html.append("</p>");
        }
        html.append("</header>");
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
        html.append("<ul>");
        model.declaredSkills().forEach(skill -> html.append("<li>")
                .append(escape(skill.skillName()))
                .append(" — ")
                .append(escape(skill.competencyLevel()))
                .append("</li>"));
        html.append("</ul></section>");
    }

    private void appendExperience(StringBuilder html, CvDocumentModel model) {
        if (model.experiences().isEmpty()) return;
        sectionStart(html, "Work Experience");
        model.experiences().forEach(item -> {
            html.append("<article><h3>").append(escape(item.positionTitle())).append(" — ")
                    .append(escape(item.organization())).append("</h3>");
            appendMeta(html, dateRange(item.startDate(), item.endDate(), item.currentRole()), item.location());
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendProjects(StringBuilder html, CvDocumentModel model) {
        if (model.projects().isEmpty()) return;
        sectionStart(html, "Projects");
        model.projects().forEach(item -> {
            html.append("<article><h3>").append(escape(item.title())).append("</h3>");
            if (item.startDate() != null || item.endDate() != null) appendMeta(html, dateRange(item.startDate(), item.endDate(), false), null);
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            if (!item.skills().isEmpty()) {
                StringJoiner skills = new StringJoiner(", ");
                item.skills().forEach(skill -> skills.add(escape(skill.skillName())));
                html.append("<p><strong>Technologies:</strong> ").append(skills).append("</p>");
            }
            appendLink(html, "Repository", item.repositoryUrl());
            appendLink(html, "Demo", item.demoUrl());
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendCertificates(StringBuilder html, CvDocumentModel model) {
        if (model.certificates().isEmpty()) return;
        sectionStart(html, "Certificates");
        model.certificates().forEach(item -> {
            html.append("<article><h3>").append(escape(item.title())).append("</h3>");
            appendMeta(html, formatDate(item.issueDate()), item.issuer());
            appendLink(html, "Credential", item.credentialUrl());
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendAwards(StringBuilder html, CvDocumentModel model) {
        if (model.awards().isEmpty()) return;
        sectionStart(html, "Awards and Honors");
        model.awards().forEach(item -> {
            html.append("<article><h3>").append(escape(item.title())).append("</h3>");
            appendMeta(html, formatDate(item.awardDate()), item.issuer());
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendActivities(StringBuilder html, CvDocumentModel model) {
        if (model.activities().isEmpty()) return;
        sectionStart(html, "Extracurricular Activities");
        model.activities().forEach(item -> {
            html.append("<article><h3>").append(escape(item.activityName())).append(" — ")
                    .append(escape(item.roleTitle())).append("</h3>");
            appendMeta(html, dateRange(item.startDate(), item.endDate(), item.endDate() == null && item.startDate() != null), null);
            if (hasText(item.description())) html.append("<p>").append(escape(item.description())).append("</p>");
            html.append("</article>");
        });
        html.append("</section>");
    }

    private void appendAcademics(StringBuilder html, CvDocumentModel model) {
        if (model.academicSummary() == null) return;
        sectionStart(html, "Academic Summary");
        html.append("<p>Computer Science GPA: ").append(escape(model.academicSummary().computerScienceGpa().toPlainString()));
        if (model.academicSummary().totalCredits() != null) {
            html.append(" &middot; Credits: ").append(escape(model.academicSummary().totalCredits().toPlainString()));
        }
        html.append("</p></section>");
    }

    private void sectionStart(StringBuilder html, String title) {
        html.append("<section><h2>").append(escape(title)).append("</h2>");
    }

    private void appendMeta(StringBuilder html, String first, String second) {
        StringJoiner joiner = new StringJoiner(" &middot; ");
        if (hasText(first)) joiner.add(escape(first));
        if (hasText(second)) joiner.add(escape(second));
        String value = joiner.toString();
        if (!value.isEmpty()) html.append("<p class=\"cv-meta\">").append(value).append("</p>");
    }

    private void appendLink(StringBuilder html, String label, String url) {
        String href = safeWebUrl(url);
        if (href == null) return;
        html.append("<p><a href=\"").append(escape(href)).append("\" rel=\"noopener noreferrer\">")
                .append(escape(label)).append("</a></p>");
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
