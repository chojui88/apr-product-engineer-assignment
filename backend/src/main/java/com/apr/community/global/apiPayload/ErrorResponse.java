package com.apr.community.global.apiPayload;

import com.apr.community.global.apiPayload.code.BaseErrorCode;

/**
 * 공통 에러 응답: { "code", "message" }
 * 성공 응답은 과제가 지정한 형태를 그대로 반환해야 해서 ApiResponse 로 감싸지 않고, 에러에만 사용한다.
 */
public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(BaseErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }
}
