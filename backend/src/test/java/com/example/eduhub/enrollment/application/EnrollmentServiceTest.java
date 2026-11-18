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
import com.example.eduhub.enrollment.domain.CancelDeadlinePassedException;
import com.example.eduhub.enrollment.domain.DuplicateEnrollmentException;
import com.example.eduhub.enrollment.domain.Enrollment;
import com.example.eduhub.enrollment.domain.EnrollmentAccessDeniedException;
import com.example.eduhub.enrollment.domain.EnrollmentRepository;
import com.example.eduhub.enrollment.domain.EnrollmentStatus;
import com.example.eduhub.enrollment.domain.InvalidEnrollmentStatusException;
import com.example.eduhub.enrollment.domain.SessionNotEnrollableException;
import com.example.eduhub.enrollment.domain.WaitlistFullException;
import java.time.Clock;
import java.time.Instant;
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
  private static final Long ENROLLMENT_ID = 100L;
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
    session = course.addSession(TODAY.plusDays(7), "본사 3층", 4); // 정원 4, 대기 한도 2
  }

  private void sessionLocked() {
    given(courseSessionRepository.findByIdForUpdate(SESSION_ID)).willReturn(Optional.of(session));
  }

  private void counts(long confirmed, long waiting) {
    given(enrollmentRepository.countBySessionIdAndStatus(SESSION_ID, EnrollmentStatus.CONFIRMED))
        .willReturn(confirmed);
    if (confirmed >= session.getCapacity()) {
      given(enrollmentRepository.countBySessionIdAndStatus(SESSION_ID, EnrollmentStatus.WAITING))
          .willReturn(waiting);
    }
  }

  private void saveReturnsArgument() {
    given(enrollmentRepository.save(any(Enrollment.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  @DisplayName("대기 인원 한도는 정원의 50%(소수점 버림)")
  void waitlistLimit() {
    assertThat(EnrollmentService.waitlistLimit(20)).isEqualTo(10);
    assertThat(EnrollmentService.waitlistLimit(5)).isEqualTo(2);
    assertThat(EnrollmentService.waitlistLimit(1)).isZero();
  }

  @Test
  @DisplayName("FR-04: 자리가 있으면 확정되고 순번은 없다")
  void enroll_withSeat_confirmed() {
    sessionLocked();
    counts(3, 0);
    saveReturnsArgument();

    EnrollmentResponse response = service.enroll(SESSION_ID, ME);

    assertThat(response.status()).isEqualTo("CONFIRMED");
    assertThat(response.waitingNumber()).isNull();
  }

  @Test
  @DisplayName("FR-05: 정원이 차면 대기로 등록되고 순번을 알려 준다")
  void enroll_full_waitingWithNumber() {
    sessionLocked();
    counts(4, 1);
    saveReturnsArgument();

    EnrollmentResponse response = service.enroll(SESSION_ID, ME);

    assertThat(response.status()).isEqualTo("WAITING");
    assertThat(response.waitingNumber()).isEqualTo(2);
  }

  @Test
  @DisplayName("D-02: 대기 인원이 정원의 50%면 더 받지 않는다")
  void enroll_waitlistFull_throws() {
    sessionLocked();
    counts(4, 2);

    assertThatThrownBy(() -> service.enroll(SESSION_ID, ME))
        .isInstanceOf(WaitlistFullException.class);
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

  private Enrollment existing(EnrollmentStatus status, Long owner) {
    given(enrollmentRepository.findSessionIdById(ENROLLMENT_ID))
        .willReturn(Optional.of(SESSION_ID));
    sessionLocked();
    Enrollment enrollment =
        status == EnrollmentStatus.WAITING
            ? Enrollment.waiting(session, owner, Instant.EPOCH)
            : Enrollment.confirmed(session, owner, Instant.EPOCH);
    given(enrollmentRepository.findById(ENROLLMENT_ID)).willReturn(Optional.of(enrollment));
    return enrollment;
  }

  @Test
  @DisplayName("FR-06: 확정 신청을 취소하면 대기 1순위가 확정된다")
  void cancel_confirmed_promotesFirstWaiting() {
    Enrollment mine = existing(EnrollmentStatus.CONFIRMED, ME);
    Enrollment first = Enrollment.waiting(session, 2L, Instant.EPOCH);
    given(
            enrollmentRepository.findFirstBySessionIdAndStatusOrderByIdAsc(
                SESSION_ID, EnrollmentStatus.WAITING))
        .willReturn(Optional.of(first));

    EnrollmentResponse response = service.cancel(ENROLLMENT_ID, ME);

    assertThat(response.status()).isEqualTo("CANCELED");
    assertThat(mine.getCanceledAt()).isNotNull();
    assertThat(first.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
  }

  @Test
  @DisplayName("FR-06: 대기자가 없으면 취소만 된다")
  void cancel_confirmed_noWaiting() {
    existing(EnrollmentStatus.CONFIRMED, ME);
    given(
            enrollmentRepository.findFirstBySessionIdAndStatusOrderByIdAsc(
                SESSION_ID, EnrollmentStatus.WAITING))
        .willReturn(Optional.empty());

    assertThat(service.cancel(ENROLLMENT_ID, ME).status()).isEqualTo("CANCELED");
  }

  @Test
  @DisplayName("대기 신청을 취소하면 승급은 일어나지 않는다")
  void cancel_waiting_noPromotion() {
    existing(EnrollmentStatus.WAITING, ME);

    service.cancel(ENROLLMENT_ID, ME);

    verify(enrollmentRepository, never()).findFirstBySessionIdAndStatusOrderByIdAsc(any(), any());
  }

  @Test
  @DisplayName("NFR-03: 다른 사람의 신청은 취소할 수 없다")
  void cancel_notOwner_throws() {
    existing(EnrollmentStatus.CONFIRMED, 2L);

    assertThatThrownBy(() -> service.cancel(ENROLLMENT_ID, ME))
        .isInstanceOf(EnrollmentAccessDeniedException.class);
  }

  @Test
  @DisplayName("D-01: 회차 시작 1일 전까지는 취소할 수 있다")
  void cancel_dayBeforeStart_allowed() {
    session = course.addSession(TODAY.plusDays(1), "본사 3층", 4);
    existing(EnrollmentStatus.WAITING, ME);

    assertThat(service.cancel(ENROLLMENT_ID, ME).status()).isEqualTo("CANCELED");
  }

  @Test
  @DisplayName("D-01: 회차 시작 당일에는 취소할 수 없다")
  void cancel_onStartDay_throws() {
    session = course.addSession(TODAY, "본사 3층", 4);
    existing(EnrollmentStatus.CONFIRMED, ME);

    assertThatThrownBy(() -> service.cancel(ENROLLMENT_ID, ME))
        .isInstanceOf(CancelDeadlinePassedException.class);
  }

  @Test
  @DisplayName("이미 취소한 신청은 다시 취소할 수 없다")
  void cancel_twice_throws() {
    existing(EnrollmentStatus.WAITING, ME).cancel(Instant.EPOCH);

    assertThatThrownBy(() -> service.cancel(ENROLLMENT_ID, ME))
        .isInstanceOf(InvalidEnrollmentStatusException.class);
  }
}
