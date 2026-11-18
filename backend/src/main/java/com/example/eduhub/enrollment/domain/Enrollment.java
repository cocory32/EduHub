package com.example.eduhub.enrollment.domain;

import com.example.eduhub.course.domain.CourseSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** 회차 수강 신청. 상태 변경은 setter 대신 메서드로만 한다. */
@Entity
@Table(name = "enrollment")
public class Enrollment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "session_id", nullable = false)
  private CourseSession session;

  @Column(name = "employee_id", nullable = false)
  private Long employeeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EnrollmentStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "canceled_at")
  private Instant canceledAt;

  protected Enrollment() {}

  private Enrollment(
      CourseSession session, Long employeeId, EnrollmentStatus status, Instant createdAt) {
    this.session = session;
    this.employeeId = employeeId;
    this.status = status;
    this.createdAt = createdAt;
  }

  /** 정원 안의 신청. 바로 확정된다 (FR-04). */
  public static Enrollment confirmed(CourseSession session, Long employeeId, Instant now) {
    return new Enrollment(session, employeeId, EnrollmentStatus.CONFIRMED, now);
  }

  public Long getId() {
    return id;
  }

  public CourseSession getSession() {
    return session;
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public EnrollmentStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getCanceledAt() {
    return canceledAt;
  }
}
