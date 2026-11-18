package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SubjectRequest(
    @NotBlank(message = "Название предмета не может быть пустым")
        @Size(max = 60, message = "Название не должно превышать 60 символов")
        String name,
    List<Long> teacherIds,
    List<Long> groupIds) {}
