package com.example.eduhub.course.domain;

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
import java.time.LocalDate;

/** 과정의 회차. 정원(capacity)은 회차 단위로 관리한다. */
@Entity
@Table(name = "course_session")
public class CourseSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(nullable = false, length = 100)
  private String place;

  @Column(nullable = false)
  private int capacity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SessionStatus status = SessionStatus.OPEN;

  protected CourseSession() {}

  CourseSession(Course course, LocalDate startDate, String place, int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException("정원은 1 이상이어야 합니다");
    }
    this.course = course;
    this.startDate = startDate;
    this.place = place;
    this.capacity = capacity;
  }

  public Long getId() {
    return id;
  }

  public Course getCourse() {
    return course;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public String getPlace() {
    return place;
  }

  public int getCapacity() {
    return capacity;
  }

  public SessionStatus getStatus() {
    return status;
  }
}
