package com.example.eduhub.course.api;

import com.example.eduhub.common.error.ErrorResponse;
import com.example.eduhub.course.api.dto.CourseDetailResponse;
import com.example.eduhub.course.api.dto.CourseRequest;
import com.example.eduhub.course.api.dto.SessionRequest;
import com.example.eduhub.course.api.dto.SessionResponse;
import com.example.eduhub.course.application.CourseCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 과정·회차 관리 API (FR-03). 관리자 권한 구분은 인증 도입 후에 한다. */
@Tag(name = "Course", description = "교육과정 조회·관리")
@RestController
@RequestMapping("/api/v1/courses")
public class CourseAdminController {

  private final CourseCommandService courseCommandService;

  public CourseAdminController(CourseCommandService courseCommandService) {
    this.courseCommandService = courseCommandService;
  }

  @Operation(summary = "과정 등록")
  @ApiResponse(responseCode = "201", description = "등록됨")
  @ApiResponse(
      responseCode = "400",
      description = "입력값 오류",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseDetailResponse createCourse(@Valid @RequestBody CourseRequest request) {
    return courseCommandService.create(request);
  }

  @Operation(summary = "과정 수정", description = "종료된 과정은 수정할 수 없다 (409)")
  @ApiResponse(responseCode = "200", description = "수정됨")
  @ApiResponse(
      responseCode = "404",
      description = "과정이 없음",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(
      responseCode = "409",
      description = "종료된 과정",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PutMapping("/{id}")
  public CourseDetailResponse updateCourse(
      @PathVariable Long id, @Valid @RequestBody CourseRequest request) {
    return courseCommandService.update(id, request);
  }

  @Operation(summary = "회차 추가", description = "정원은 1 이상. 종료된 과정에는 추가할 수 없다 (409)")
  @ApiResponse(responseCode = "201", description = "추가됨")
  @ApiResponse(
      responseCode = "409",
      description = "종료된 과정",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/{id}/sessions")
  @ResponseStatus(HttpStatus.CREATED)
  public SessionResponse addSession(
      @PathVariable Long id, @Valid @RequestBody SessionRequest request) {
    return courseCommandService.addSession(id, request);
  }

  @Operation(summary = "과정 종료")
  @ApiResponse(responseCode = "200", description = "종료됨")
  @ApiResponse(
      responseCode = "404",
      description = "과정이 없음",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/{id}/close")
  public CourseDetailResponse closeCourse(@PathVariable Long id) {
    return courseCommandService.close(id);
  }
}
