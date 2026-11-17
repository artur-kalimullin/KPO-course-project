package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TeacherCreateRequest(
    @NotBlank @Size(min = 6, max = 40) String username,
    String password,
    @NotBlank
        @Size(max = 60)
        @Pattern(
            regexp = "^[А-Яа-яЁёA-Za-z-]+$",
            message = "Имя может содержать только буквы и дефис")
        String firstName,
    @NotBlank
        @Size(max = 60)
        @Pattern(
            regexp = "^[А-Яа-яЁёA-Za-z-]+$",
            message = "Фамилия может содержать только буквы и дефис")
        String lastName,
    @Size(max = 60)
        @Pattern(
            regexp = "^$|^[А-Яа-яЁёA-Za-z-]+$",
            message = "Отчество может содержать только буквы и дефис")
        String middleName,
    List<Long> groupIds) {}
