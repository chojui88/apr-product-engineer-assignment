package com.apr.community.domain.post.exception;

import com.apr.community.global.apiPayload.exception.GeneralException;

public class PostException extends GeneralException {

    public PostException(PostErrorCode code) {
        super(code);
    }
}
