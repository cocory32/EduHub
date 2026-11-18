package com.example.eduhub.enrollment.domain;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  boolean existsBySessionIdAndEmployeeIdAndStatusIn(
      Long sessionId, Long employeeId, Collection<EnrollmentStatus> statuses);

  long countBySessionIdAndStatus(Long sessionId, EnrollmentStatus status);
}
