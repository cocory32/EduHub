package com.example.eduhub.course.domain;

/** 과정 상태. 종료(CLOSED)된 과정은 수정하거나 회차를 추가할 수 없다 (FR-03). */
public enum CourseStatus {
  OPEN,
  CLOSED
}
