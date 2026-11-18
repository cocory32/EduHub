package com.example.eduhub.course.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** 회차 추가 요청 (FR-03). 정원은 1 이상. */
public record SessionRequest(
    @NotNull LocalDate startDate, @NotBlank @Size(max = 100) String place, @Min(1) int capacity) {}
