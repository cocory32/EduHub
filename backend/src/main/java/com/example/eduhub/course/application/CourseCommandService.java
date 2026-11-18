package com.example.eduhub.course.application;

import com.example.eduhub.course.api.dto.CourseDetailResponse;
import com.example.eduhub.course.api.dto.CourseRequest;
import com.example.eduhub.course.api.dto.SessionRequest;
import com.example.eduhub.course.api.dto.SessionResponse;
import com.example.eduhub.course.domain.Course;
import com.example.eduhub.course.domain.CourseNotFoundException;
import com.example.eduhub.course.domain.CourseRepository;
import com.example.eduhub.course.domain.CourseSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 과정·회차 등록, 수정, 종료 (FR-03). 관리자 권한 확인은 인증 도입(4~5주차) 후에 추가한다. */
@Service
@Transactional
public class CourseCommandService {

  private final CourseRepository courseRepository;

  public CourseCommandService(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
  }

  public CourseDetailResponse create(CourseRequest request) {
    Course course =
        courseRepository.save(
            new Course(request.title(), request.category(), request.description()));
    return CourseDetailResponse.from(course);
  }

  public CourseDetailResponse update(Long id, CourseRequest request) {
    Course course = findCourse(id);
    course.update(request.title(), request.category(), request.description());
    return CourseDetailResponse.from(course);
  }

  public SessionResponse addSession(Long courseId, SessionRequest request) {
    Course course = findCourse(courseId);
    CourseSession session =
        course.addSession(request.startDate(), request.place(), request.capacity());
    courseRepository.flush(); // 새 회차의 id를 응답에 담기 위해 바로 저장한다
    return SessionResponse.from(session);
  }

  public CourseDetailResponse close(Long id) {
    Course course = findCourse(id);
    course.close();
    return CourseDetailResponse.from(course);
  }

  private Course findCourse(Long id) {
    return courseRepository
        .findWithSessionsById(id)
        .orElseThrow(() -> new CourseNotFoundException(id));
  }
}
