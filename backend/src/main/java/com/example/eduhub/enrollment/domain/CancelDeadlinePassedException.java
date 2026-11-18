package com.example.eduhub.enrollment.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 회차 시작 1일 전이 지나 취소하려 할 때. 409로 응답한다 (D-01). */
public class CancelDeadlinePassedException extends BusinessException {

  public CancelDeadlinePassedException(Long id) {
    super(HttpStatus.CONFLICT, "CANCEL_DEADLINE_PASSED", "취소 기한(회차 시작 1일 전)이 지났습니다: " + id);
  }
}
