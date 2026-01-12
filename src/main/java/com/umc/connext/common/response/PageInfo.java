package com.umc.connext.common.response;

public record PageInfo(
        int page,
        int size,
        boolean hasNext,
        long totalElements,
        int totalPages
) {}