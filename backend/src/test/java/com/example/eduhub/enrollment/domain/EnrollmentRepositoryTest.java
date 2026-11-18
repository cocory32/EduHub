package com.example.eduhub.enrollment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.eduhub.course.domain.Course;
import com.example.eduhub.course.domain.CourseRepository;
import com.example.eduhub.course.domain.CourseSession;
import jakarta.persistence.EntityManager;
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

/** 실제 PostgreSQL(.env의 TEST_DB_*)에서 인덱스와 순번 쿼리를 확인한다. 각 테스트는 끝나면 롤백된다. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EnrollmentRepositoryTest {

  @Autowired private EnrollmentRepository enrollmentRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private EntityManager entityManager;

  private CourseSession session;

  @BeforeEach
  void setUp() {
    Course course = new Course("Spring Boot 입문", "BACKEND", null);
    session = course.addSession(LocalDate.of(2026, 12, 1), "본사 3층", 2);
    courseRepository.saveAndFlush(course);
  }

  @Test
  @DisplayName("FR-04: 같은 회차에 진행 중인 신청을 두 번 넣으면 유니크 인덱스가 막는다")
  void activeDuplicate_rejectedByIndex() {
    enrollmentRepository.saveAndFlush(Enrollment.confirmed(session, 1L, Instant.now()));

    assertThatThrownBy(
            () -> enrollmentRepository.saveAndFlush(Enrollment.waiting(session, 1L, Instant.now())))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("취소한 뒤에는 같은 회차에 다시 신청할 수 있다")
  void reEnrollAfterCancel_allowed() {
    Enrollment first =
        enrollmentRepository.saveAndFlush(Enrollment.confirmed(session, 1L, Instant.now()));
    first.cancel(Instant.now());
    enrollmentRepository.saveAndFlush(first);

    enrollmentRepository.saveAndFlush(Enrollment.confirmed(session, 1L, Instant.now()));

    assertThat(
            enrollmentRepository.existsBySessionIdAndEmployeeIdAndStatusIn(
                session.getId(), 1L, EnrollmentStatus.ACTIVE))
        .isTrue();
  }

  @Test
  @DisplayName("FR-05/06: 대기 순번과 1순위는 신청 순서(id)로 정한다")
  void waitingOrder() {
    Enrollment w1 = enrollmentRepository.save(Enrollment.waiting(session, 11L, Instant.now()));
    Enrollment w2 = enrollmentRepository.save(Enrollment.waiting(session, 12L, Instant.now()));
    Enrollment w3 = enrollmentRepository.save(Enrollment.waiting(session, 13L, Instant.now()));
    w1.cancel(Instant.now());
    entityManager.flush();
    entityManager.clear();

    assertThat(
            enrollmentRepository.countAhead(session.getId(), EnrollmentStatus.WAITING, w3.getId()))
        .isEqualTo(1); // w1이 빠졌으므로 w3은 2번
    assertThat(
            enrollmentRepository
                .findFirstBySessionIdAndStatusOrderByIdAsc(
                    session.getId(), EnrollmentStatus.WAITING)
                .map(Enrollment::getId))
        .contains(w2.getId());
    assertThat(enrollmentRepository.findSessionIdById(w3.getId())).contains(session.getId());
  }
}
