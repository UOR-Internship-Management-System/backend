package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request;

import java.util.UUID;

/** Raw API query criteria validated by the application service before database access. */
public record InternshipRequestSearchCriteria(
        Integer page,
        Integer size,
        String sort,
        String search,
        UUID companyId) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final int MAX_SEARCH_LENGTH = 120;
}
