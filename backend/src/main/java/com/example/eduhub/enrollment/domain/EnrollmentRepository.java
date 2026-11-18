package com.example.eduhub.enrollment.domain;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  boolean existsBySessionIdAndEmployeeIdAndStatusIn(
      Long sessionId, Long employeeId, Collection<EnrollmentStatus> statuses);

  long countBySessionIdAndStatus(Long sessionId, EnrollmentStatus status);

  /** 같은 회차에서 이 신청보다 먼저(id가 작은) 대기 중인 신청 수. 대기 순번 = 이 값 + 1 (FR-05). */
  @Query(
      """
      select count(e) from Enrollment e
      where e.session.id = :sessionId and e.status = :status and e.id < :id
      """)
  long countAhead(
      @Param("sessionId") Long sessionId,
      @Param("status") EnrollmentStatus status,
      @Param("id") Long id);

  /** 대기 1순위. 승급 대상이다 (FR-06). */
  Optional<Enrollment> findFirstBySessionIdAndStatusOrderByIdAsc(
      Long sessionId, EnrollmentStatus status);

  /** 엔티티를 영속성 컨텍스트에 올리지 않고 회차 id만 읽는다. 회차 락을 잡은 뒤 신청을 새로 읽기 위해 쓴다. */
  @Query("select e.session.id from Enrollment e where e.id = :id")
  Optional<Long> findSessionIdById(@Param("id") Long id);
}
