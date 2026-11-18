package com.example.eduhub.enrollment.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eduhub.enrollment.api.dto.EnrollmentResponse;
import com.example.eduhub.enrollment.application.EnrollmentService;
import com.example.eduhub.enrollment.domain.CancelDeadlinePassedException;
import com.example.eduhub.enrollment.domain.EnrollmentAccessDeniedException;
import com.example.eduhub.enrollment.domain.WaitlistFullException;
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
  @DisplayName("정원이 찬 회차에 신청하면 201과 대기 순번을 돌려준다")
  void enroll_waiting_returns201() throws Exception {
    given(enrollmentService.enroll(10L, 1L))
        .willReturn(new EnrollmentResponse(100L, 10L, "WAITING", 3));

    mockMvc
        .perform(post("/api/v1/sessions/10/enrollments").header(HEADER, "1"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("WAITING"))
        .andExpect(jsonPath("$.waitingNumber").value(3));
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
  @DisplayName("대기 인원까지 찼으면 409 WAITLIST_FULL")
  void enroll_waitlistFull_returns409() throws Exception {
    given(enrollmentService.enroll(10L, 1L)).willThrow(new WaitlistFullException(10L));

    mockMvc
        .perform(post("/api/v1/sessions/10/enrollments").header(HEADER, "1"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("WAITLIST_FULL"));
  }

  @Test
  @DisplayName("신청 조회는 현재 대기 순번을 돌려준다")
  void get_returnsWaitingNumber() throws Exception {
    given(enrollmentService.get(100L, 1L))
        .willReturn(new EnrollmentResponse(100L, 10L, "WAITING", 1));

    mockMvc
        .perform(get("/api/v1/enrollments/100").header(HEADER, "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.waitingNumber").value(1));
  }

  @Test
  @DisplayName("취소하면 200과 CANCELED")
  void cancel_returns200() throws Exception {
    given(enrollmentService.cancel(100L, 1L))
        .willReturn(new EnrollmentResponse(100L, 10L, "CANCELED", null));

    mockMvc
        .perform(post("/api/v1/enrollments/100/cancel").header(HEADER, "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"));
  }

  @Test
  @DisplayName("다른 사람 신청을 취소하면 403")
  void cancel_notOwner_returns403() throws Exception {
    given(enrollmentService.cancel(100L, 2L)).willThrow(new EnrollmentAccessDeniedException(100L));

    mockMvc
        .perform(post("/api/v1/enrollments/100/cancel").header(HEADER, "2"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ENROLLMENT_FORBIDDEN"));
  }

  @Test
  @DisplayName("취소 기한이 지나면 409 CANCEL_DEADLINE_PASSED")
  void cancel_deadlinePassed_returns409() throws Exception {
    given(enrollmentService.cancel(100L, 1L)).willThrow(new CancelDeadlinePassedException(100L));

    mockMvc
        .perform(post("/api/v1/enrollments/100/cancel").header(HEADER, "1"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CANCEL_DEADLINE_PASSED"));
  }
}
