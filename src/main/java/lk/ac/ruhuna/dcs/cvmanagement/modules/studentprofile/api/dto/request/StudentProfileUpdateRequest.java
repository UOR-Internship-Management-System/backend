package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Partial Student Profile update request that preserves the distinction between an omitted field
 * and an explicitly supplied {@code null}. Nullable fields (headline, summary, phone, location) can
 * therefore be cleared without a missing key being mistaken for "leave this field untouched".
 */
public final class StudentProfileUpdateRequest {

    @Size(max = 150)
    private String fullName;

    @Email
    @Size(max = 254)
    private String personalEmail;

    @Size(max = 200)
    private String headline;

    private String summary;

    @Size(max = 30)
    private String phone;

    @Size(max = 150)
    private String location;

    private boolean fullNamePresent;
    private boolean personalEmailPresent;
    private boolean headlinePresent;
    private boolean summaryPresent;
    private boolean phonePresent;
    private boolean locationPresent;

    @JsonSetter("fullName")
    public void setFullName(String fullName) {
        this.fullNamePresent = true;
        this.fullName = fullName;
    }

    @JsonSetter("personalEmail")
    public void setPersonalEmail(String personalEmail) {
        this.personalEmailPresent = true;
        this.personalEmail = personalEmail;
    }

    @JsonSetter("headline")
    public void setHeadline(String headline) {
        this.headlinePresent = true;
        this.headline = headline;
    }

    @JsonSetter("summary")
    public void setSummary(String summary) {
        this.summaryPresent = true;
        this.summary = summary;
    }

    @JsonSetter("phone")
    public void setPhone(String phone) {
        this.phonePresent = true;
        this.phone = phone;
    }

    @JsonSetter("location")
    public void setLocation(String location) {
        this.locationPresent = true;
        this.location = location;
    }

    public String fullName() {
        return fullName;
    }

    public String personalEmail() {
        return personalEmail;
    }

    public String headline() {
        return headline;
    }

    public String summary() {
        return summary;
    }

    public String phone() {
        return phone;
    }

    public String location() {
        return location;
    }

    @JsonIgnore
    public boolean hasFullName() {
        return fullNamePresent;
    }

    @JsonIgnore
    public boolean hasPersonalEmail() {
        return personalEmailPresent;
    }

    @JsonIgnore
    public boolean hasHeadline() {
        return headlinePresent;
    }

    @JsonIgnore
    public boolean hasSummary() {
        return summaryPresent;
    }

    @JsonIgnore
    public boolean hasPhone() {
        return phonePresent;
    }

    @JsonIgnore
    public boolean hasLocation() {
        return locationPresent;
    }

    @JsonIgnore
    public boolean hasAnyField() {
        return fullNamePresent
                || personalEmailPresent
                || headlinePresent
                || summaryPresent
                || phonePresent
                || locationPresent;
    }
}
