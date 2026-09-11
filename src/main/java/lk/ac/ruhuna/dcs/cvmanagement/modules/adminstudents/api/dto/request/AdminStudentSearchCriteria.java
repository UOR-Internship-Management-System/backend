package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.RegisteredStudentSort;

/**
 * Validated query criteria for the registered-Student roster.
 *
 * <p>Defaults and normalization are kept here so controller and application layers use one contract.
 */
public record AdminStudentSearchCriteria(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        String sort,
        @Size(max = 120) String search,
        @Min(3) @Max(4) Integer level) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    public int pageOrDefault() {
        return page == null ? DEFAULT_PAGE : page;
    }

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public String normalizedSearch() {
        if (search == null) {
            return null;
        }
        String normalized = search.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public RegisteredStudentSort parsedSort() {
        return RegisteredStudentSort.fromApiValue(sort);
    }
}
