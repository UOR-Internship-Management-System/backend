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
    public static final String ADMIN_COMPANIES = API_V1 + "/admin/companies";
    public static final String ADMIN_INTERNSHIP_REQUESTS = API_V1 + "/admin/internship-requests";
    public static final String ADMIN_CANDIDATE_FILTERING_RUNS = API_V1 + "/admin/candidate-filtering/runs";
    public static final String ME_PROJECTS = API_V1 + "/me/projects";
    public static final String ME_ACADEMIC_RECORDS = API_V1 + "/me/academic-records";
    public static final String ME_ACADEMIC_RECORDS_GPA = ME_ACADEMIC_RECORDS + "/gpa";
    public static final String ME_CV = API_V1 + "/me/cv";

    private ApiPaths() {
    }
}
