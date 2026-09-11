package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request;

/** Server-side Company list controls. */
public record CompanySearchCriteria(
        Integer page,
        Integer size,
        String sort,
        String search) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final int MAX_SEARCH_LENGTH = 120;
}
