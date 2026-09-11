package lk.ac.ruhuna.dcs.cvmanagement.shared.pagination;

import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequestFactory {

    private PageRequestFactory() {
    }

    public static Pageable build(Integer page, Integer size, String sort) {
        return build(page, size, sort, null);
    }

    public static Pageable build(Integer page, Integer size, String sort, Map<String, String> sortAliases) {
        int p = page != null ? page : 0;
        int s = size != null ? Math.min(size, 100) : 20;
        return PageRequest.of(p, s, parseSort(sort, sortAliases));
    }

    public static String describeSort(String sort) {
        return sort != null ? sort : "unsorted";
    }

    private static Sort parseSort(String sort, Map<String, String> sortAliases) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        if (sortAliases != null && sortAliases.containsKey(property)) {
            property = sortAliases.get(property);
        } else if (sortAliases != null) {
            return Sort.unsorted();
        }
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
