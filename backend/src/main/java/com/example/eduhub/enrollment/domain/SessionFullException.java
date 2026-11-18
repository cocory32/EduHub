package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 정원이 다 찼을 때. 409로 응답한다 (FR-04). 대기 등록은 FR-05에서 추가한다. */
public class SessionFullException extends BusinessException {

  public SessionFullException(Long sessionId) {
    super(HttpStatus.CONFLICT, "SESSION_FULL", "정원이 찼습니다: " + sessionId);
  }
}
