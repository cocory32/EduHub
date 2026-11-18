package com.example.eduhub.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.eduhub.course.domain.Course;
import com.example.eduhub.course.domain.CourseSession;
import com.example.eduhub.course.domain.CourseSessionRepository;
import com.example.eduhub.course.domain.SessionNotFoundException;
import com.example.eduhub.enrollment.api.dto.EnrollmentResponse;
import com.example.eduhub.enrollment.domain.DuplicateEnrollmentException;
import com.example.eduhub.enrollment.domain.Enrollment;
import com.example.eduhub.enrollment.domain.EnrollmentRepository;
import com.example.eduhub.enrollment.domain.EnrollmentStatus;
import com.example.eduhub.enrollment.domain.SessionFullException;
import com.example.eduhub.enrollment.domain.SessionNotEnrollableException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

  private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
  private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);
  private static final Long SESSION_ID = 10L;
  private static final Long ME = 1L;

  @Mock private EnrollmentRepository enrollmentRepository;
  @Mock private CourseSessionRepository courseSessionRepository;

  private EnrollmentService service;
  private Course course;
  private CourseSession session;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);
    service = new EnrollmentService(enrollmentRepository, courseSessionRepository, clock);
    course = new Course("Spring Boot 입문", "BACKEND", null);
    session = course.addSession(TODAY.plusDays(7), "본사 3층", 4);
  }

  private void sessionLocked() {
    given(courseSessionRepository.findByIdForUpdate(SESSION_ID)).willReturn(Optional.of(session));
  }

  @Test
  @DisplayName("FR-04: 자리가 있으면 확정된다")
  void enroll_withSeat_confirmed() {
    sessionLocked();
    given(enrollmentRepository.countBySessionIdAndStatus(SESSION_ID, EnrollmentStatus.CONFIRMED))
        .willReturn(3L);
    given(enrollmentRepository.save(any(Enrollment.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    EnrollmentResponse response = service.enroll(SESSION_ID, ME);

    assertThat(response.status()).isEqualTo("CONFIRMED");
  }

  @Test
  @DisplayName("FR-04: 정원이 찼으면 SessionFullException")
  void enroll_full_throws() {
    sessionLocked();
    given(enrollmentRepository.countBySessionIdAndStatus(SESSION_ID, EnrollmentStatus.CONFIRMED))
        .willReturn(4L);

    assertThatThrownBy(() -> service.enroll(SESSION_ID, ME))
        .isInstanceOf(SessionFullException.class);
    verify(enrollmentRepository, never()).save(any());
  }

  @Test
  @DisplayName("FR-04: 같은 회차에 진행 중인 신청이 있으면 중복")
  void enroll_duplicate_throws() {
    sessionLocked();
    given(
            enrollmentRepository.existsBySessionIdAndEmployeeIdAndStatusIn(
                SESSION_ID, ME, EnrollmentStatus.ACTIVE))
        .willReturn(true);

    assertThatThrownBy(() -> service.enroll(SESSION_ID, ME))
        .isInstanceOf(DuplicateEnrollmentException.class);
  }

  @Test
  @DisplayName("종료된 과정의 회차에는 신청할 수 없다")
  void enroll_closedCourse_throws() {
    course.close();
    sessionLocked();

    assertThatThrownBy(() -> service.enroll(SESSION_ID, ME))
        .isInstanceOf(SessionNotEnrollableException.class);
  }

  @Test
  @DisplayName("없는 회차에 신청하면 SessionNotFoundException")
  void enroll_missingSession_throws() {
    given(courseSessionRepository.findByIdForUpdate(SESSION_ID)).willReturn(Optional.empty());

    assertThatThrownBy(() -> service.enroll(SESSION_ID, ME))
        .isInstanceOf(SessionNotFoundException.class);
  }
}
