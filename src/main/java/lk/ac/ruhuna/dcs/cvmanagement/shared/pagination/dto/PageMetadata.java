package lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto;

public record PageMetadata(int page, int size, long totalElements, int totalPages, String sort) {
}
