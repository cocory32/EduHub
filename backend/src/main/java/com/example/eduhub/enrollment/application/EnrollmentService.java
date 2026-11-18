package com.example.eduhub.enrollment.application;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수강 신청 (FR-04).
 *
 * <p>정합성(NFR-01): 신청은 먼저 회차 행에 비관적 락을 잡는다. 같은 회차의 정원 계산이 DB에서 한 줄로 실행되므로 서버가 여러 대여도 정원을 넘지 않는다. 중복
 * 신청은 부분 유니크 인덱스(uq_enrollment_active_session_employee)가 한 번 더 막는다.
 */
@Service
@Transactional
public class EnrollmentService {

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

  /** 회차에 신청한다. 자리가 있으면 확정하고, 정원이 찼으면 거절한다. */
  public EnrollmentResponse enroll(Long sessionId, Long employeeId) {
    CourseSession session =
        courseSessionRepository
            .findByIdForUpdate(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
    if (!session.isEnrollable()) {
      throw new SessionNotEnrollableException(sessionId);
    }
    if (enrollmentRepository.existsBySessionIdAndEmployeeIdAndStatusIn(
        sessionId, employeeId, EnrollmentStatus.ACTIVE)) {
      throw new DuplicateEnrollmentException(sessionId);
    }

    long confirmed =
        enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.CONFIRMED);
    if (confirmed >= session.getCapacity()) {
      throw new SessionFullException(sessionId);
    }
    Enrollment enrollment =
        enrollmentRepository.save(Enrollment.confirmed(session, employeeId, clock.instant()));
    return new EnrollmentResponse(enrollment.getId(), sessionId, enrollment.getStatus().name());
  }
}
