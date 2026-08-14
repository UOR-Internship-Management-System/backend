package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request;

/** Query controls for one registered Student's committed official academic records. */
public record AdminAcademicRecordCriteria(
        Integer page,
        Integer size,
        String sort,
        String search,
        String courseCode) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final int MAX_SEARCH_LENGTH = 120;
    public static final int MAX_COURSE_CODE_LENGTH = 30;
}
