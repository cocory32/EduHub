package com.example.eduhub.common.health;

import io.swagger.v3.oas.annotations.media.Schema;

/** 서버 상태 확인 응답. */
public record HealthResponse(@Schema(description = "서버 상태", example = "UP") String status) {}
