package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 종료된 회차나 과정에 신청할 때. 409로 응답한다 (FR-04). */
public class SessionNotEnrollableException extends BusinessException {

  public SessionNotEnrollableException(Long sessionId) {
    super(HttpStatus.CONFLICT, "SESSION_CLOSED", "신청을 받지 않는 회차입니다: " + sessionId);
  }
}
