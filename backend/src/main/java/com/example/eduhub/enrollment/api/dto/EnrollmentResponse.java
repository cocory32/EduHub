package com.example.eduhub.enrollment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 수강 신청 결과 (FR-04). */
public record EnrollmentResponse(
    Long id,
    Long sessionId,
    @Schema(description = "CONFIRMED, WAITING, CANCELED, COMPLETED", example = "CONFIRMED")
        String status) {}
