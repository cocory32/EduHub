package com.example.eduhub.course.api.dto;

import com.example.eduhub.course.domain.Course;
import java.util.List;

/** 과정 상세와 회차 목록 (FR-02). */
public record CourseDetailResponse(
    Long id,
    String title,
    String category,
    String description,
    String status,
    List<SessionResponse> sessions) {

  public static CourseDetailResponse from(Course course) {
    return new CourseDetailResponse(
        course.getId(),
        course.getTitle(),
        course.getCategory(),
        course.getDescription(),
        course.getStatus().name(),
        course.getSessions().stream().map(SessionResponse::from).toList());
  }
}
