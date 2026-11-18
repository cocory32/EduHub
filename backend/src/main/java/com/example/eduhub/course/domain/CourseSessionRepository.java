package com.example.eduhub.course.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {

  /**
   * 회차 행에 비관적 쓰기 락(SELECT ... FOR UPDATE)을 건다. 같은 회차의 신청·취소를 DB에서 한 줄로 세워 정원 초과를 막는다 (NFR-01). 과정
   * 행까지 잠그지 않도록 fetch join을 쓰지 않는다.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from CourseSession s where s.id = :id")
  Optional<CourseSession> findByIdForUpdate(@Param("id") Long id);
}
