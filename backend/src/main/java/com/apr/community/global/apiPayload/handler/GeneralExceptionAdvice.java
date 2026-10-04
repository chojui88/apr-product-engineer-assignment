package com.apr.community.global.apiPayload.handler;

import com.apr.community.global.apiPayload.ErrorResponse;
import com.apr.community.global.apiPayload.exception.GeneralException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GeneralExceptionAdvice {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException e) {
        // TODO(#3) e.getBaseErrorCode() 의 status 로 ErrorResponse.of(...) 반환
        throw new UnsupportedOperationException("TODO(#3)");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // TODO(#3) GeneralErrorCode.METHOD_ARGUMENT_NOT_VALID → 400
        throw new UnsupportedOperationException("TODO(#3)");
    }

    // TODO(#3) HttpMessageNotReadableException → JSON_PARSE_ERROR
    // TODO(#3) MethodArgumentTypeMismatchException(경로 id 타입) → TYPE_OR_FORMAT_NOT_VALID
    // TODO(#3) HandlerMethodValidationException(size 범위 등) → VALID_FAIL
}
