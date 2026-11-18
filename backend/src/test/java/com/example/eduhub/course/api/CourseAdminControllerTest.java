package com.example.eduhub.course.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eduhub.course.api.dto.CourseRequest;
import com.example.eduhub.course.application.CourseCommandService;
import com.example.eduhub.course.domain.CourseClosedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseAdminController.class)
@ActiveProfiles("test")
class CourseAdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CourseCommandService courseCommandService;

  @Test
  @DisplayName("제목 없이 등록하면 400과 VALIDATION_FAILED")
  void createCourse_blankTitle_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"category\":\"BACKEND\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("종료된 과정을 수정하면 409와 COURSE_CLOSED")
  void updateCourse_closed_returns409() throws Exception {
    given(courseCommandService.update(eq(1L), any(CourseRequest.class)))
        .willThrow(new CourseClosedException(1L));

    mockMvc
        .perform(
            put("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"새 제목\",\"category\":\"BACKEND\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("COURSE_CLOSED"));
  }

  @Test
  @DisplayName("정원 0으로 회차를 추가하면 400")
  void addSession_zeroCapacity_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/courses/1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"startDate\":\"2026-12-01\",\"place\":\"본사 3층\",\"capacity\":0}"))
        .andExpect(status().isBadRequest());
  }
}
