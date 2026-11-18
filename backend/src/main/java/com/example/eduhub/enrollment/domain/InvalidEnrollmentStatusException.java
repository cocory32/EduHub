package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 현재 상태에서 할 수 없는 상태 변경을 요청했을 때. 409로 응답한다. */
public class InvalidEnrollmentStatusException extends BusinessException {

  public InvalidEnrollmentStatusException(Long id, EnrollmentStatus status) {
    super(
        HttpStatus.CONFLICT,
        "INVALID_ENROLLMENT_STATUS",
        "현재 상태(" + status + ")에서는 할 수 없는 요청입니다: " + id);
  }
}
