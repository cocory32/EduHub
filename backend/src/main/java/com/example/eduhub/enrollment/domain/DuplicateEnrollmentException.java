package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 같은 회차에 진행 중인 신청이 이미 있을 때. 409로 응답한다 (FR-04). */
public class DuplicateEnrollmentException extends BusinessException {

  public DuplicateEnrollmentException(Long sessionId) {
    super(HttpStatus.CONFLICT, "DUPLICATE_ENROLLMENT", "이미 신청한 회차입니다: " + sessionId);
  }
}
