package com.apr.community.global.pagination;

import java.util.List;

/** 목록 봉투: items · total(조건에 맞는 전체 개수) · nextCursor(마지막 페이지면 null) */
public record PageResponse<T>(
        List<T> items,
        long total,
        String nextCursor
) {
}
