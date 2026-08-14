package lk.ac.ruhuna.dcs.cvmanagement.shared.api;

public final class ApiPaths {

    public static final String API_V1 = "/api/v1";
    public static final String HEALTH = API_V1 + "/health";
    public static final String ME_PROFILE = API_V1 + "/me/profile";
    public static final String SKILL_TAXONOMY = API_V1 + "/skill-taxonomy";
    public static final String ME_DECLARED_SKILLS = API_V1 + "/me/declared-skills";
    public static final String ADMIN_ACADEMIC_LEDGER_UPLOADS = API_V1 + "/admin/academic-ledger/uploads";
    public static final String ADMIN_ACADEMIC_RECORDS = API_V1 + "/admin/academic-records";
    public static final String ADMIN_STUDENTS = API_V1 + "/admin/students";
    public static final String ME_PROJECTS = API_V1 + "/me/projects";

    private ApiPaths() {
    }
}
