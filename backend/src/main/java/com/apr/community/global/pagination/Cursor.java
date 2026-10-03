package com.apr.community.global.pagination;

import java.time.Instant;

/** 커서 = 마지막 항목의 (createdAt, id). 외부엔 불투명 문자열로 노출 */
public record Cursor(Instant createdAt, Long id) {

    public String encode() {
        // TODO(#4) Base64URL("createdAt|id")
        throw new UnsupportedOperationException("TODO(#4)");
    }

    public static Cursor decode(String value) {
        // TODO(#4) 잘못된 값이면 new GeneralException(GeneralErrorCode.INVALID_CURSOR)
        throw new UnsupportedOperationException("TODO(#4)");
    }
}
