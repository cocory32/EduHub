package com.example.eduhub.course.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 과정 등록·수정 요청 (FR-03). */
public record CourseRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 50) String category,
    String description) {}
