package com.example.eduhub.enrollment.api;

import com.example.eduhub.common.error.ErrorResponse;
import com.example.eduhub.enrollment.api.dto.EnrollmentResponse;
import com.example.eduhub.enrollment.application.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 수강 신청 API (FR-04).
 *
 * <p>인증 도입(4~5주차) 전까지는 로그인 사용자를 {@value #EMPLOYEE_HEADER} 헤더로 받는다. 헤더가 없으면 401로 응답한다.
 */
@Tag(name = "Enrollment", description = "수강 신청·대기·취소")
@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {

  static final String EMPLOYEE_HEADER = "X-Employee-Id";

  private final EnrollmentService enrollmentService;

  public EnrollmentController(EnrollmentService enrollmentService) {
    this.enrollmentService = enrollmentService;
  }

  @Operation(summary = "수강 신청", description = "자리가 있으면 CONFIRMED로 확정한다. 같은 회차에 중복 신청할 수 없다")
  @ApiResponse(responseCode = "201", description = "확정됨")
  @ApiResponse(
      responseCode = "401",
      description = "사용자 헤더 없음",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "회차가 없음",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "중복 신청, 종료된 회차, 정원 마감",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/sessions/{sessionId}/enrollments")
  @ResponseStatus(HttpStatus.CREATED)
  public EnrollmentResponse enroll(
      @PathVariable Long sessionId,
      @Parameter(description = "신청자 사번(인증 도입 전 임시)") @RequestHeader(EMPLOYEE_HEADER)
          Long employeeId) {
    return enrollmentService.enroll(sessionId, employeeId);
  }
}
