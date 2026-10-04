package com.apr.community.global.pagination;

import com.apr.community.global.apiPayload.code.GeneralErrorCode;
import com.apr.community.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorTest {

    private static String encodeRaw(String raw) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes());
    }

    @Test
    void 인코딩_후_디코딩하면_같은_값() {
        Cursor cursor = new Cursor(Instant.parse("2026-01-01T00:00:00Z"), 42L);

        Cursor decoded = Cursor.decode(cursor.encode());

        assertThat(decoded).isEqualTo(cursor);
    }

    @Test
    void 잘못된_Base64면_INVALID_CURSOR() {
        assertThatThrownBy(() -> Cursor.decode("not-base64!!"))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getBaseErrorCode())
                .isEqualTo(GeneralErrorCode.INVALID_CURSOR);
    }

    @Test
    void 구분자가_없으면_INVALID_CURSOR() {
        String broken = encodeRaw("no-separator-here");

        assertThatThrownBy(() -> Cursor.decode(broken))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getBaseErrorCode())
                .isEqualTo(GeneralErrorCode.INVALID_CURSOR);
    }

    @Test
    void id가_0이하면_INVALID_CURSOR() {
        String broken = encodeRaw("2026-01-01T00:00:00Z|0");

        assertThatThrownBy(() -> Cursor.decode(broken))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getBaseErrorCode())
                .isEqualTo(GeneralErrorCode.INVALID_CURSOR);
    }

    @Test
    void 생성시각이_파싱불가면_INVALID_CURSOR() {
        String broken = encodeRaw("invalid-instant|1");

        assertThatThrownBy(() -> Cursor.decode(broken))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getBaseErrorCode())
                .isEqualTo(GeneralErrorCode.INVALID_CURSOR);
    }
}
