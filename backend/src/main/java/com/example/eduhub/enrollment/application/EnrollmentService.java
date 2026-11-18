package com.example.eduhub.enrollment.application;

import com.example.eduhub.course.domain.CourseSession;
import com.example.eduhub.course.domain.CourseSessionRepository;
import com.example.eduhub.course.domain.SessionNotFoundException;
import com.example.eduhub.enrollment.api.dto.EnrollmentResponse;
import com.example.eduhub.enrollment.domain.CancelDeadlinePassedException;
import com.example.eduhub.enrollment.domain.DuplicateEnrollmentException;
import com.example.eduhub.enrollment.domain.Enrollment;
import com.example.eduhub.enrollment.domain.EnrollmentAccessDeniedException;
import com.example.eduhub.enrollment.domain.EnrollmentNotFoundException;
import com.example.eduhub.enrollment.domain.EnrollmentRepository;
import com.example.eduhub.enrollment.domain.EnrollmentStatus;
import com.example.eduhub.enrollment.domain.SessionNotEnrollableException;
import com.example.eduhub.enrollment.domain.WaitlistFullException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수강 신청(FR-04), 대기 순번(FR-05), 취소와 대기 자동 승급(FR-06).
 *
 * <p>정합성(NFR-01): 신청과 취소는 모두 먼저 회차 행에 비관적 락을 잡는다. 같은 회차의 정원 계산·승급이 DB에서 한 줄로 실행되므로 서버가 여러 대여도 정원을
 * 넘지 않는다. 중복 신청은 부분 유니크 인덱스(uq_enrollment_active_session_employee)가 한 번 더 막는다.
 */
@Service
@Transactional
public class EnrollmentService {

  private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

  private final EnrollmentRepository enrollmentRepository;
  private final CourseSessionRepository courseSessionRepository;
  private final Clock clock;

  public EnrollmentService(
      EnrollmentRepository enrollmentRepository,
      CourseSessionRepository courseSessionRepository,
      Clock clock) {
    this.enrollmentRepository = enrollmentRepository;
    this.courseSessionRepository = courseSessionRepository;
    this.clock = clock;
  }

  /** 대기 인원 한도. 정원의 50%까지 받는다 (D-02). 소수점은 버린다. */
  static int waitlistLimit(int capacity) {
    return capacity / 2;
  }

  /** 회차에 신청한다. 자리가 있으면 확정, 정원이 찼으면 대기로 등록하고 순번을 돌려준다. */
  public EnrollmentResponse enroll(Long sessionId, Long employeeId) {
    CourseSession session = lockSession(sessionId);
    if (!session.isEnrollable()) {
      throw new SessionNotEnrollableException(sessionId);
    }
    if (enrollmentRepository.existsBySessionIdAndEmployeeIdAndStatusIn(
        sessionId, employeeId, EnrollmentStatus.ACTIVE)) {
      throw new DuplicateEnrollmentException(sessionId);
    }

    Instant now = clock.instant();
    long confirmed =
        enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.CONFIRMED);
    if (confirmed < session.getCapacity()) {
      Enrollment enrollment =
          enrollmentRepository.save(Enrollment.confirmed(session, employeeId, now));
      return toResponse(enrollment, null);
    }

    long waiting =
        enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.WAITING);
    if (waiting >= waitlistLimit(session.getCapacity())) {
      throw new WaitlistFullException(sessionId);
    }
    Enrollment enrollment = enrollmentRepository.save(Enrollment.waiting(session, employeeId, now));
    // 락 안에서 센 값이므로 새 대기자의 순번은 기존 대기 인원 + 1이다.
    return toResponse(enrollment, (int) waiting + 1);
  }

  /** 본인 신청을 조회한다. 대기 중이면 현재 순번을 함께 돌려준다 (FR-05). */
  @Transactional(readOnly = true)
  public EnrollmentResponse get(Long enrollmentId, Long employeeId) {
    Enrollment enrollment = findOwned(enrollmentId, employeeId);
    return toResponse(enrollment, currentWaitingNumber(enrollment));
  }

  /**
   * 본인 신청을 취소한다. 회차 시작 1일 전까지만 가능하다 (D-01). 확정 신청을 취소하면 대기 1순위를 확정으로 승급한다 (FR-06).
   *
   * @return 취소된 신청
   */
  public EnrollmentResponse cancel(Long enrollmentId, Long employeeId) {
    // 신청 엔티티를 락 전에 읽으면 오래된 상태를 볼 수 있으므로, 회차 id만 먼저 읽고 락을 잡은 뒤 신청을 읽는다.
    Long sessionId =
        enrollmentRepository
            .findSessionIdById(enrollmentId)
            .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
    CourseSession session = lockSession(sessionId);
    Enrollment enrollment = findOwned(enrollmentId, employeeId);

    LocalDate deadline = session.getStartDate().minusDays(1);
    if (LocalDate.now(clock).isAfter(deadline)) {
      throw new CancelDeadlinePassedException(enrollmentId);
    }

    boolean wasConfirmed = enrollment.getStatus() == EnrollmentStatus.CONFIRMED;
    enrollment.cancel(clock.instant());
    if (wasConfirmed) {
      promoteFirstWaiting(sessionId);
    }
    return toResponse(enrollment, null);
  }

  private void promoteFirstWaiting(Long sessionId) {
    enrollmentRepository
        .findFirstBySessionIdAndStatusOrderByIdAsc(sessionId, EnrollmentStatus.WAITING)
        .ifPresent(
            next -> {
              next.promote();
              // FR-09(알림) 전까지는 로그로 남긴다.
              log.info(
                  "대기 승급: enrollmentId={}, sessionId={}, employeeId={}",
                  next.getId(),
                  sessionId,
                  next.getEmployeeId());
            });
  }

  private CourseSession lockSession(Long sessionId) {
    return courseSessionRepository
        .findByIdForUpdate(sessionId)
        .orElseThrow(() -> new SessionNotFoundException(sessionId));
  }

  private Enrollment findOwned(Long enrollmentId, Long employeeId) {
    Enrollment enrollment =
        enrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
    if (!enrollment.isOwnedBy(employeeId)) {
      throw new EnrollmentAccessDeniedException(enrollmentId);
    }
    return enrollment;
  }

  private Integer currentWaitingNumber(Enrollment enrollment) {
    if (enrollment.getStatus() != EnrollmentStatus.WAITING) {
      return null;
    }
    long ahead =
        enrollmentRepository.countAhead(
            enrollment.getSession().getId(), EnrollmentStatus.WAITING, enrollment.getId());
    return (int) ahead + 1;
  }

  private static EnrollmentResponse toResponse(Enrollment enrollment, Integer waitingNumber) {
    return new EnrollmentResponse(
        enrollment.getId(),
        enrollment.getSession().getId(),
        enrollment.getStatus().name(),
        waitingNumber);
  }
}
