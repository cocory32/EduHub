package com.example.eduhub.course.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.eduhub.course.api.dto.CourseDetailResponse;
import com.example.eduhub.course.api.dto.CourseRequest;
import com.example.eduhub.course.domain.Course;
import com.example.eduhub.course.domain.CourseClosedException;
import com.example.eduhub.course.domain.CourseNotFoundException;
import com.example.eduhub.course.domain.CourseRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseCommandServiceTest {

  @Mock private CourseRepository courseRepository;

  @InjectMocks private CourseCommandService courseCommandService;

  @Test
  @DisplayName("종료된 과정을 수정하면 CourseClosedException")
  void update_closedCourse_throws() {
    Course course = new Course("Spring Boot 입문", "BACKEND", null);
    course.close();
    given(courseRepository.findWithSessionsById(1L)).willReturn(Optional.of(course));

    assertThatThrownBy(
            () -> courseCommandService.update(1L, new CourseRequest("새 제목", "BACKEND", null)))
        .isInstanceOf(CourseClosedException.class);
  }

  @Test
  @DisplayName("없는 과정을 수정하면 CourseNotFoundException")
  void update_missingCourse_throws() {
    given(courseRepository.findWithSessionsById(99L)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> courseCommandService.update(99L, new CourseRequest("제목", "BACKEND", null)))
        .isInstanceOf(CourseNotFoundException.class);
  }

  @Test
  @DisplayName("과정을 등록하면 OPEN 상태로 저장된다")
  void create_savesOpenCourse() {
    given(courseRepository.save(any(Course.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    CourseDetailResponse response =
        courseCommandService.create(new CourseRequest("JPA 실전", "BACKEND", "설명"));

    assertThat(response.title()).isEqualTo("JPA 실전");
    assertThat(response.status()).isEqualTo("OPEN");
    assertThat(response.sessions()).isEmpty();
  }
}
