package lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagedResponse<T>(List<T> items, PageMetadata page) {

    public static <T> PagedResponse<T> of(Page<T> page, String sort) {
        return new PagedResponse<>(
            page.getContent(),
            new PageMetadata(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), sort));
    }
}
