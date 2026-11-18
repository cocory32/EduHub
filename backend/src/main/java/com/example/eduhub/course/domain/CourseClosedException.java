package com.example.eduhub.course.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 종료된 과정을 바꾸려 할 때. 409로 응답한다 (FR-03). */
public class CourseClosedException extends BusinessException {

  public CourseClosedException(Long id) {
    super(HttpStatus.CONFLICT, "COURSE_CLOSED", "종료된 과정은 변경할 수 없습니다: " + id);
  }
}
