package com.example.eduhub.course.domain;

import com.example.eduhub.common.error.BusinessException;
import org.springframework.http.HttpStatus;

/** 없는 회차를 참조했을 때. 404로 응답한다. */
public class SessionNotFoundException extends BusinessException {

  public SessionNotFoundException(Long id) {
    super(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "회차를 찾을 수 없습니다: " + id);
  }
}
