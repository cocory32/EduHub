package com.example.eduhub.course.api.dto;

import com.example.eduhub.course.domain.Course;

/** 과정 목록의 한 줄 (FR-01). */
public record CourseSummaryResponse(
    Long id, String title, String category, String status, int sessionCount) {

  public static CourseSummaryResponse of(Course course, long sessionCount) {
    return new CourseSummaryResponse(
        course.getId(),
        course.getTitle(),
        course.getCategory(),
        course.getStatus().name(),
        (int) sessionCount);
  }
}
