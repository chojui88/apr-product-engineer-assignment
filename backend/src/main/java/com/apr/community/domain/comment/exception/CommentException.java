package com.apr.community.domain.comment.exception;

import com.apr.community.global.apiPayload.exception.GeneralException;

public class CommentException extends GeneralException {

    public CommentException(CommentErrorCode code) {
        super(code);
    }
}
