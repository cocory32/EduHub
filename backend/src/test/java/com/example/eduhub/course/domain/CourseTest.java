package com.example.eduhub.course.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseTest {

  @Test
  @DisplayName("종료된 과정은 수정할 수 없다")
  void update_closedCourse_throws() {
    Course course = new Course("Spring Boot 입문", "BACKEND", "설명");
    course.close();

    assertThatThrownBy(() -> course.update("새 제목", "BACKEND", "설명"))
        .isInstanceOf(CourseClosedException.class);
  }

  @Test
  @DisplayName("종료된 과정에는 회차를 추가할 수 없다")
  void addSession_closedCourse_throws() {
    Course course = new Course("Spring Boot 입문", "BACKEND", null);
    course.close();

    assertThatThrownBy(() -> course.addSession(LocalDate.of(2026, 12, 1), "본사 3층", 20))
        .isInstanceOf(CourseClosedException.class);
  }

  @Test
  @DisplayName("회차 정원은 1 이상이어야 한다")
  void addSession_zeroCapacity_throws() {
    Course course = new Course("Spring Boot 입문", "BACKEND", null);

    assertThatThrownBy(() -> course.addSession(LocalDate.of(2026, 12, 1), "본사 3층", 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("열린 과정은 수정되고 회차가 추가된다")
  void update_and_addSession_openCourse() {
    Course course = new Course("Spring Boot 입문", "BACKEND", null);

    course.update("Spring Boot 실전", "BACKEND", "심화");
    course.addSession(LocalDate.of(2026, 12, 1), "본사 3층", 20);

    assertThat(course.getTitle()).isEqualTo("Spring Boot 실전");
    assertThat(course.getSessions()).hasSize(1);
    assertThat(course.getStatus()).isEqualTo(CourseStatus.OPEN);
  }
}
