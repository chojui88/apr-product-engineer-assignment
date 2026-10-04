package com.apr.community.global.apiPayload.handler;

import com.apr.community.global.apiPayload.ErrorResponse;
import com.apr.community.global.apiPayload.code.BaseErrorCode;
import com.apr.community.global.apiPayload.code.GeneralErrorCode;
import com.apr.community.global.apiPayload.exception.GeneralException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException e) {
        return toResponse(e.getBaseErrorCode());
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(GeneralErrorCode.METHOD_ARGUMENT_NOT_VALID.getMessage());
        return toResponse(GeneralErrorCode.METHOD_ARGUMENT_NOT_VALID, message);
    }

    // 400 파라미터 검증
    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handlerParameterValidation(Exception e){
        return toResponse(GeneralErrorCode.VALID_FAIL);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e){
        return toResponse(GeneralErrorCode.JSON_PARSE_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(GeneralErrorCode.TYPE_OR_FORMAT_NOT_VALID);
    }

    // 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handlerNoResource(NoResourceFoundException e){
        return toResponse(GeneralErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e){
        return toResponse(GeneralErrorCode.METHOD_NOT_ALLOWED);
    }

    //500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e){
        log.error("[Unhandled] {}", e.getMessage(), e);
        return toResponse(GeneralErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(BaseErrorCode code){
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
    }

    private ResponseEntity<ErrorResponse> toResponse(BaseErrorCode code, String message){
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, message));
    }

}
