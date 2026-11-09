package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectRequest(
    @NotBlank(message = "Название предмета не может быть пустым")
        @Size(max = 200, message = "Название не должно превышать 200 символов")
        String name) {}
