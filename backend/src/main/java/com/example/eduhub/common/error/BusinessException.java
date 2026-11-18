package com.example.eduhub.common.error;

import org.springframework.http.HttpStatus;

/** 업무 규칙 위반. 하위 클래스가 HTTP 상태와 오류 코드를 정한다. */
public abstract class BusinessException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  protected BusinessException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }
}
