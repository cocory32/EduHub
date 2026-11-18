package com.example.eduhub.course.application;

import com.example.eduhub.common.api.PageResponse;
import com.example.eduhub.course.api.dto.CourseDetailResponse;
import com.example.eduhub.course.api.dto.CourseSummaryResponse;
import com.example.eduhub.course.domain.Course;
import com.example.eduhub.course.domain.CourseNotFoundException;
import com.example.eduhub.course.domain.CourseRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 과정 조회 (FR-01, FR-02). */
@Service
@Transactional(readOnly = true)
public class CourseQueryService {

  private final CourseRepository courseRepository;

  public CourseQueryService(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
  }

  public PageResponse<CourseSummaryResponse> search(
      String keyword, String category, Pageable pageable) {
    Page<Course> courses =
        courseRepository.search(normalize(keyword), normalize(category), pageable);
    Map<Long, Long> sessionCounts = countSessions(courses.getContent());
    return PageResponse.from(
        courses.map(
            course ->
                CourseSummaryResponse.of(course, sessionCounts.getOrDefault(course.getId(), 0L))));
  }

  public CourseDetailResponse get(Long id) {
    Course course =
        courseRepository
            .findWithSessionsById(id)
            .orElseThrow(() -> new CourseNotFoundException(id));
    return CourseDetailResponse.from(course);
  }

  private Map<Long, Long> countSessions(List<Course> courses) {
    if (courses.isEmpty()) {
      return Map.of();
    }
    List<Long> ids = courses.stream().map(Course::getId).toList();
    return courseRepository.countSessionsByCourseIds(ids).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim();
  }
}
