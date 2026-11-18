package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 정원과 대기 인원(정원의 50%, D-02)이 모두 찼을 때. 409로 응답한다 (FR-05). */
public class WaitlistFullException extends BusinessException {

  public WaitlistFullException(Long sessionId) {
    super(HttpStatus.CONFLICT, "WAITLIST_FULL", "정원과 대기 인원이 모두 찼습니다: " + sessionId);
  }
}
