package com.example.eduhub.course.api.dto;

import com.example.eduhub.course.domain.CourseSession;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/** 회차 정보 (FR-02). */
public record SessionResponse(
    Long id,
    LocalDate startDate,
    String place,
    int capacity,
    @Schema(description = "남은 자리. 수강 신청(3주차) 전까지는 정원과 같다") int remainingSeats,
    String status) {

  public static SessionResponse from(CourseSession session) {
    return new SessionResponse(
        session.getId(),
        session.getStartDate(),
        session.getPlace(),
        session.getCapacity(),
        session.getCapacity(),
        session.getStatus().name());
  }
}
