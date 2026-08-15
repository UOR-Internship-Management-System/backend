package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

/** Stable event names for Admin-critical Company and Internship Request mutations. */
public enum AuditEventType {
    COMPANY_CREATED,
    COMPANY_UPDATED,
    COMPANY_DELETED,
    INTERNSHIP_REQUEST_CREATED,
    INTERNSHIP_REQUEST_UPDATED,
    INTERNSHIP_REQUEST_DELETED,
    INTERNSHIP_REQUIRED_SKILL_ADDED,
    INTERNSHIP_REQUIRED_SKILL_REMOVED,
    INTERNSHIP_REQUIRED_SKILLS_REPLACED
}
