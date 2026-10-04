package com.apr.community.global.pagination;

import com.apr.community.global.apiPayload.code.GeneralErrorCode;
import com.apr.community.global.apiPayload.exception.GeneralException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/** 커서 = 마지막 항목의 (createdAt, id). 외부엔 불투명 문자열로 노출 */
public record Cursor(Instant createdAt, Long id) {

    //정렬 기준 created_at 최신순, id 많은 순
    //첫 요청에는 커서가 없다
    // size+1 조회
    //응답의 마지막 항목 커서로 만들기
    //Cursor.encode() 두값을 문자열로 합치고 인코딩
    // 커서를 해석 -> 프론트에서 받은 nextCursor 그대로 보내고
    // decode가 문자열에서 생성 시각과 ID를 복호화 하고
    //레포지터리가 조건으로 조회!
    // 다음 페이지 없으면 커서를 주지 않음

    private static final String SEP = "|";

    public String encode() {
        String raw = createdAt.toString() + SEP + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String value) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
            int idx = raw.lastIndexOf(SEP);
            if (idx <= 0) throw new IllegalArgumentException();
            Instant createdAt = Instant.parse(raw.substring(0, idx));
            long id = Long.parseLong(raw.substring(idx + 1));
            if (id<=0) throw new IllegalArgumentException();
            return new Cursor(createdAt, id);
        } catch (IllegalArgumentException | DateTimeParseException e){
            throw new GeneralException(GeneralErrorCode.INVALID_CURSOR);
        }
    }
}
