package com.regyinventory.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtil {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "id";

    private PageableUtil() {
    }

    public static Pageable create(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    ) {
        int normalizedPage = page == null || page < 0
                ? DEFAULT_PAGE
                : page;

        int normalizedSize = size == null || size <= 0
                ? DEFAULT_SIZE
                : Math.min(size, MAX_SIZE);

        String normalizedSortBy =
                sortBy == null || sortBy.isBlank()
                        ? DEFAULT_SORT_FIELD
                        : sortBy.trim();

        Sort.Direction sortDirection =
                "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return PageRequest.of(
                normalizedPage,
                normalizedSize,
                Sort.by(sortDirection, normalizedSortBy)
        );
    }
}