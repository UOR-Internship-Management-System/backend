package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;

/** Partial update preserving omitted versus explicit-null semantics. */
public final class InternshipRequestUpdateRequest {

    private String title;
    private String description;
    private Integer shortlistGuidanceValue;
    private List<InternshipRequiredSkillRequest> requiredSkills;
    private boolean titlePresent;
    private boolean descriptionPresent;
    private boolean shortlistGuidanceValuePresent;
    private boolean requiredSkillsPresent;

    @JsonSetter("title") public void setTitle(String title) { titlePresent = true; this.title = title; }
    @JsonSetter("description") public void setDescription(String description) { descriptionPresent = true; this.description = description; }
    @JsonSetter("shortlistGuidanceValue") public void setShortlistGuidanceValue(Integer value) { shortlistGuidanceValuePresent = true; shortlistGuidanceValue = value; }
    @JsonSetter("requiredSkills") public void setRequiredSkills(List<InternshipRequiredSkillRequest> value) { requiredSkillsPresent = true; requiredSkills = value; }

    public String title() { return title; }
    public String description() { return description; }
    public Integer shortlistGuidanceValue() { return shortlistGuidanceValue; }
    public List<InternshipRequiredSkillRequest> requiredSkills() { return requiredSkills; }
    @JsonIgnore public boolean hasTitle() { return titlePresent; }
    @JsonIgnore public boolean hasDescription() { return descriptionPresent; }
    @JsonIgnore public boolean hasShortlistGuidanceValue() { return shortlistGuidanceValuePresent; }
    @JsonIgnore public boolean hasRequiredSkills() { return requiredSkillsPresent; }
    @JsonIgnore public boolean hasAnyField() { return titlePresent || descriptionPresent || shortlistGuidanceValuePresent || requiredSkillsPresent; }
}
