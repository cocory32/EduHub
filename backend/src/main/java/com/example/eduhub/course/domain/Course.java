package com.example.eduhub.course.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 교육과정. 상태 변경은 setter 대신 update(), addSession(), close() 메서드로만 한다. */
@Entity
@Table(name = "course")
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 50)
  private String category;

  @Column(columnDefinition = "text")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CourseStatus status = CourseStatus.OPEN;

  @OneToMany(mappedBy = "course", cascade = CascadeType.PERSIST)
  @OrderBy("startDate ASC")
  private List<CourseSession> sessions = new ArrayList<>();

  protected Course() {}

  public Course(String title, String category, String description) {
    this.title = title;
    this.category = category;
    this.description = description;
  }

  /** 과정 정보를 수정한다. 종료된 과정은 수정할 수 없다 (FR-03). */
  public void update(String title, String category, String description) {
    ensureOpen();
    this.title = title;
    this.category = category;
    this.description = description;
  }

  /** 회차를 추가한다. 종료된 과정에는 추가할 수 없다 (FR-03). */
  public CourseSession addSession(LocalDate startDate, String place, int capacity) {
    ensureOpen();
    CourseSession session = new CourseSession(this, startDate, place, capacity);
    sessions.add(session);
    return session;
  }

  /** 과정을 종료한다. */
  public void close() {
    this.status = CourseStatus.CLOSED;
  }

  private void ensureOpen() {
    if (status == CourseStatus.CLOSED) {
      throw new CourseClosedException(id);
    }
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getCategory() {
    return category;
  }

  public String getDescription() {
    return description;
  }

  public CourseStatus getStatus() {
    return status;
  }

  public List<CourseSession> getSessions() {
    return Collections.unmodifiableList(sessions);
  }
}
