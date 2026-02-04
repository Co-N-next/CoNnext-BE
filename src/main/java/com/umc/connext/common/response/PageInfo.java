package com.umc.connext.common.response;

import org.springframework.data.domain.Page;


public record PageInfo(
        int page,
        int size,
        boolean hasNext,
        long totalElements,
        int totalPages
) {
    public static PageInfo of(Page<?> page) {
        return new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
