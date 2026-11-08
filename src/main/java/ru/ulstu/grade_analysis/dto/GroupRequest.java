package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRequest(
    @NotBlank(message = "Название группы не может быть пустым")
        @Size(max = 50, message = "Название не должно превышать 50 символов")
        String name) {}
