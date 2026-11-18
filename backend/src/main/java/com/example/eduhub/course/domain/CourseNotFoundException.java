package com.example.eduhub.course.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 없는 과정을 조회했을 때. 404로 응답한다. */
public class CourseNotFoundException extends BusinessException {

  public CourseNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "과정을 찾을 수 없습니다: " + id);
  }
}
