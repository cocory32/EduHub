package com.example.eduhub.course.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eduhub.common.api.PageResponse;
import com.example.eduhub.course.api.dto.CourseSummaryResponse;
import com.example.eduhub.course.application.CourseQueryService;
import com.example.eduhub.course.domain.CourseNotFoundException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseController.class)
@ActiveProfiles("test")
class CourseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CourseQueryService courseQueryService;

  @Test
  @DisplayName("과정 목록을 페이지 형식으로 돌려준다")
  void getCourses_returnsPage() throws Exception {
    given(courseQueryService.search(eq("spring"), eq(null), any(Pageable.class)))
        .willReturn(
            new PageResponse<>(
                List.of(new CourseSummaryResponse(1L, "Spring Boot 입문", "BACKEND", "OPEN", 2)),
                0,
                10,
                1));

    mockMvc
        .perform(get("/api/v1/courses").param("keyword", "spring").param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("Spring Boot 입문"))
        .andExpect(jsonPath("$.content[0].sessionCount").value(2))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("없는 과정을 조회하면 404와 에러 코드를 돌려준다")
  void getCourse_notFound_returns404() throws Exception {
    given(courseQueryService.get(99L)).willThrow(new CourseNotFoundException(99L));

    mockMvc
        .perform(get("/api/v1/courses/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
  }
}
