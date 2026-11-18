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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 수강 신청 API (FR-04~06).
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

  @Operation(
      summary = "수강 신청",
      description = "자리가 있으면 CONFIRMED, 정원이 찼으면 WAITING과 대기 순번을 돌려준다. 대기는 정원의 50%까지 받는다")
  @ApiResponse(responseCode = "201", description = "신청됨(확정 또는 대기)")
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
      description = "중복 신청, 종료된 회차, 대기 인원 초과",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/sessions/{sessionId}/enrollments")
  @ResponseStatus(HttpStatus.CREATED)
  public EnrollmentResponse enroll(
      @PathVariable Long sessionId,
      @Parameter(description = "신청자 사번(인증 도입 전 임시)") @RequestHeader(EMPLOYEE_HEADER)
          Long employeeId) {
    return enrollmentService.enroll(sessionId, employeeId);
  }

  @Operation(summary = "신청 조회", description = "본인 신청의 상태와 현재 대기 순번을 조회한다")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @ApiResponse(
      responseCode = "403",
      description = "본인 신청이 아님",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "신청이 없음",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @GetMapping("/enrollments/{id}")
  public EnrollmentResponse getEnrollment(
      @PathVariable Long id,
      @Parameter(description = "신청자 사번(인증 도입 전 임시)") @RequestHeader(EMPLOYEE_HEADER)
          Long employeeId) {
    return enrollmentService.get(id, employeeId);
  }

  @Operation(
      summary = "신청 취소",
      description = "회차 시작 1일 전까지 취소할 수 있다. 확정 신청을 취소하면 대기 1순위가 자동으로 확정된다")
  @ApiResponse(responseCode = "200", description = "취소됨")
  @ApiResponse(
      responseCode = "403",
      description = "본인 신청이 아님",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "신청이 없음",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "취소 기한 지남, 이미 취소·수료된 신청",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/enrollments/{id}/cancel")
  public EnrollmentResponse cancel(
      @PathVariable Long id,
      @Parameter(description = "신청자 사번(인증 도입 전 임시)") @RequestHeader(EMPLOYEE_HEADER)
          Long employeeId) {
    return enrollmentService.cancel(id, employeeId);
  }
}
