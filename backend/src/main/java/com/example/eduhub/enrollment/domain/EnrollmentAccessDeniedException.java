package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 다른 사람의 신청을 조회·취소하려 할 때. 403으로 응답한다 (NFR-03). */
public class EnrollmentAccessDeniedException extends BusinessException {

  public EnrollmentAccessDeniedException(Long id) {
    super(HttpStatus.FORBIDDEN, "ENROLLMENT_FORBIDDEN", "본인의 신청만 조회·취소할 수 있습니다: " + id);
  }
}
