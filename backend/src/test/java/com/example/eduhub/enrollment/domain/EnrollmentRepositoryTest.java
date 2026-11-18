package com.example.eduhub.enrollment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.eduhub.course.domain.Course;
import com.example.eduhub.course.domain.CourseRepository;
import com.example.eduhub.course.domain.CourseSession;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

/** 실제 PostgreSQL(.env의 TEST_DB_*)에서 인덱스와 집계 쿼리를 확인한다. 각 테스트는 끝나면 롤백된다. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EnrollmentRepositoryTest {

  @Autowired private EnrollmentRepository enrollmentRepository;
  @Autowired private CourseRepository courseRepository;

  private CourseSession session;

  @BeforeEach
  void setUp() {
    Course course = new Course("Spring Boot 입문", "BACKEND", null);
    session = course.addSession(LocalDate.of(2026, 12, 1), "본사 3층", 2);
    courseRepository.saveAndFlush(course);
  }

  @Test
  @DisplayName("회차별 확정 인원과 본인 신청 여부를 조회한다")
  void countAndExists() {
    enrollmentRepository.saveAndFlush(Enrollment.confirmed(session, 1L, Instant.now()));
    enrollmentRepository.saveAndFlush(Enrollment.confirmed(session, 2L, Instant.now()));

    assertThat(
            enrollmentRepository.countBySessionIdAndStatus(
                session.getId(), EnrollmentStatus.CONFIRMED))
        .isEqualTo(2);
    assertThat(
            enrollmentRepository.existsBySessionIdAndEmployeeIdAndStatusIn(
                session.getId(), 1L, EnrollmentStatus.ACTIVE))
        .isTrue();
    assertThat(
            enrollmentRepository.existsBySessionIdAndEmployeeIdAndStatusIn(
                session.getId(), 3L, EnrollmentStatus.ACTIVE))
        .isFalse();
  }

  @Test
  @DisplayName("FR-04: 같은 회차에 진행 중인 신청을 두 번 넣으면 유니크 인덱스가 막는다")
  void activeDuplicate_rejectedByIndex() {
    enrollmentRepository.saveAndFlush(Enrollment.confirmed(session, 1L, Instant.now()));

    assertThatThrownBy(
            () ->
                enrollmentRepository.saveAndFlush(Enrollment.confirmed(session, 1L, Instant.now())))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
