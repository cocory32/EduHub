package com.example.eduhub.common.error;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 예외를 ErrorResponse 형식으로 바꾼다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
    return ResponseEntity.status(e.getStatus())
        .body(new ErrorResponse(e.getCode(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return new ErrorResponse("VALIDATION_FAILED", message);
  }

  /** 인증 도입 전 임시: 사용자 식별 헤더가 없으면 로그인하지 않은 것으로 본다 (NFR-03). */
  @ExceptionHandler(MissingRequestHeaderException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponse handleMissingHeader(MissingRequestHeaderException e) {
    return new ErrorResponse("UNAUTHENTICATED", "로그인이 필요합니다: " + e.getHeaderName() + " 헤더 없음");
  }
}
