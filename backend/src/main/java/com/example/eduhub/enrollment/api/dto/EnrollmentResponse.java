package com.example.eduhub.enrollment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 수강 신청 결과 (FR-04~06). */
public record EnrollmentResponse(
    Long id,
    Long sessionId,
    @Schema(description = "CONFIRMED, WAITING, CANCELED, COMPLETED", example = "WAITING")
        String status,
    @Schema(description = "대기 순번(1부터). 대기 상태가 아니면 null", example = "2", nullable = true)
        Integer waitingNumber) {}
