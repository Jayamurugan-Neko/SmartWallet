package com.smartwallet.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse.ErrorDetails details = ErrorResponse.ErrorDetails.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .traceId(UUID.randomUUID().toString())
                .build();
        return new ResponseEntity<>(new ErrorResponse(details), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse.ErrorDetails details = ErrorResponse.ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message(ex.getMessage())
                .traceId(UUID.randomUUID().toString())
                .build();
        return new ResponseEntity<>(new ErrorResponse(details), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
