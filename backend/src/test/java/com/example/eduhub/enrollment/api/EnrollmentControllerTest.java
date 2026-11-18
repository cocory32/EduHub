package com.example.eduhub.enrollment.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eduhub.enrollment.api.dto.EnrollmentResponse;
import com.example.eduhub.enrollment.application.EnrollmentService;
import com.example.eduhub.enrollment.domain.DuplicateEnrollmentException;
import com.example.eduhub.enrollment.domain.SessionFullException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EnrollmentController.class)
@ActiveProfiles("test")
class EnrollmentControllerTest {

  private static final String HEADER = EnrollmentController.EMPLOYEE_HEADER;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EnrollmentService enrollmentService;

  @Test
  @DisplayName("신청하면 201과 CONFIRMED")
  void enroll_returns201() throws Exception {
    given(enrollmentService.enroll(10L, 1L))
        .willReturn(new EnrollmentResponse(100L, 10L, "CONFIRMED"));

    mockMvc
        .perform(post("/api/v1/sessions/10/enrollments").header(HEADER, "1"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  @DisplayName("사용자 헤더가 없으면 401")
  void enroll_withoutHeader_returns401() throws Exception {
    mockMvc
        .perform(post("/api/v1/sessions/10/enrollments"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
  }

  @Test
  @DisplayName("중복 신청이면 409 DUPLICATE_ENROLLMENT")
  void enroll_duplicate_returns409() throws Exception {
    given(enrollmentService.enroll(10L, 1L)).willThrow(new DuplicateEnrollmentException(10L));

    mockMvc
        .perform(post("/api/v1/sessions/10/enrollments").header(HEADER, "1"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_ENROLLMENT"));
  }

  @Test
  @DisplayName("정원이 찼으면 409 SESSION_FULL")
  void enroll_full_returns409() throws Exception {
    given(enrollmentService.enroll(10L, 1L)).willThrow(new SessionFullException(10L));

    mockMvc
        .perform(post("/api/v1/sessions/10/enrollments").header(HEADER, "1"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SESSION_FULL"));
  }
}
