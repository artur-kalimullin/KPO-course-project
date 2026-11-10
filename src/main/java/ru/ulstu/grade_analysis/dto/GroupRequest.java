package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;

public record GroupRequest(
    @NotBlank(message = "Название группы не может быть пустым") @Size(max = 20) String prefix,
    @NotBlank(message = "Курс обязателен") @Size(max = 1) String course,
    @NotBlank(message = "Подгруппа обязательна")
        @Pattern(regexp = "^[1-9]$", message = "Подгруппа должна быть цифрой от 1 до 9")
        String subgroup,
    @NotNull(message = "Уровень образования обязателен") EducationLevel educationLevel) {}
