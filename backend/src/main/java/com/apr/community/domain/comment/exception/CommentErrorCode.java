package com.apr.community.domain.comment.exception;

import com.apr.community.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {

    BLANK_CONTENT(HttpStatus.BAD_REQUEST, "COMMENT400_1", "댓글 내용은 필수 입력값입니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT403_1", "본인이 작성한 댓글만 삭제할 수 있습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT404_1", "댓글을 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
