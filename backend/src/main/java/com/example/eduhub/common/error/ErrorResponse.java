package com.example.eduhub.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

/** 모든 오류 응답의 공통 형식. */
public record ErrorResponse(
    @Schema(description = "오류 코드", example = "COURSE_NOT_FOUND") String code,
    @Schema(description = "사람이 읽을 메시지") String message) {}
