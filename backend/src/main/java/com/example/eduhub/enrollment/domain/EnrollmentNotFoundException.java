package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 없는 신청을 참조했을 때. 404로 응답한다. */
public class EnrollmentNotFoundException extends BusinessException {

  public EnrollmentNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "ENROLLMENT_NOT_FOUND", "신청을 찾을 수 없습니다: " + id);
  }
}
