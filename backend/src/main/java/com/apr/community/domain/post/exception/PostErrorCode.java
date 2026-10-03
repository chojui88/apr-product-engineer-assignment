package com.apr.community.domain.post.exception;

import com.apr.community.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    INVALID_TITLE_LENGTH(HttpStatus.BAD_REQUEST, "POST400_1", "제목은 20자 이하로 입력해 주세요."),
    BLANK_TITLE_OR_CONTENT(HttpStatus.BAD_REQUEST, "POST400_2", "제목과 내용은 필수 입력값입니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST403_1", "본인이 작성한 게시글만 수정·삭제할 수 있습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST404_1", "게시글을 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
