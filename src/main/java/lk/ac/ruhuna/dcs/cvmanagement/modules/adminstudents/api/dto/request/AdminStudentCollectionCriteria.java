package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request;

/** Query controls shared by Admin declared-skill and project inspection endpoints. */
public record AdminStudentCollectionCriteria(
        Integer page,
        Integer size,
        String search) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final int MAX_SEARCH_LENGTH = 120;
}
