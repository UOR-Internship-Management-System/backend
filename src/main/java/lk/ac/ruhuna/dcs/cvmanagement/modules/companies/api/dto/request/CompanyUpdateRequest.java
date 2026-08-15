package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Partial Company update request that preserves the distinction between an omitted field and an
 * explicitly supplied {@code null}. Nullable metadata can therefore be cleared without accidentally
 * overwriting omitted values.
 */
public final class CompanyUpdateRequest {

    private String name;
    private String websiteUrl;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private String notes;

    private boolean namePresent;
    private boolean websiteUrlPresent;
    private boolean contactPersonPresent;
    private boolean contactEmailPresent;
    private boolean contactPhonePresent;
    private boolean notesPresent;

    @JsonSetter("name")
    public void setName(String name) {
        this.namePresent = true;
        this.name = name;
    }

    @JsonSetter("websiteUrl")
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrlPresent = true;
        this.websiteUrl = websiteUrl;
    }

    @JsonSetter("contactPerson")
    public void setContactPerson(String contactPerson) {
        this.contactPersonPresent = true;
        this.contactPerson = contactPerson;
    }

    @JsonSetter("contactEmail")
    public void setContactEmail(String contactEmail) {
        this.contactEmailPresent = true;
        this.contactEmail = contactEmail;
    }

    @JsonSetter("contactPhone")
    public void setContactPhone(String contactPhone) {
        this.contactPhonePresent = true;
        this.contactPhone = contactPhone;
    }

    @JsonSetter("notes")
    public void setNotes(String notes) {
        this.notesPresent = true;
        this.notes = notes;
    }

    public String name() {
        return name;
    }

    public String websiteUrl() {
        return websiteUrl;
    }

    public String contactPerson() {
        return contactPerson;
    }

    public String contactEmail() {
        return contactEmail;
    }

    public String contactPhone() {
        return contactPhone;
    }

    public String notes() {
        return notes;
    }

    @JsonIgnore
    public boolean hasName() {
        return namePresent;
    }

    @JsonIgnore
    public boolean hasWebsiteUrl() {
        return websiteUrlPresent;
    }

    @JsonIgnore
    public boolean hasContactPerson() {
        return contactPersonPresent;
    }

    @JsonIgnore
    public boolean hasContactEmail() {
        return contactEmailPresent;
    }

    @JsonIgnore
    public boolean hasContactPhone() {
        return contactPhonePresent;
    }

    @JsonIgnore
    public boolean hasNotes() {
        return notesPresent;
    }

    @JsonIgnore
    public boolean hasAnyField() {
        return namePresent
                || websiteUrlPresent
                || contactPersonPresent
                || contactEmailPresent
                || contactPhonePresent
                || notesPresent;
    }
}
