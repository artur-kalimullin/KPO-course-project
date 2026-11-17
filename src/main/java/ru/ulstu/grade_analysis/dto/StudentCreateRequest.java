package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentCreateRequest(
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
    @NotBlank @Size(max = 1) @Pattern(regexp = "^[1-5]$", message = "Курс должен быть от 1 до 5")
        String course,
    @NotNull Long groupId,
    @NotBlank @Pattern(regexp = "\\d{6}", message = "Номер зачетки должен состоять из 6 цифр")
        String recordBookNumber,
    @NotBlank String educationForm,
    @NotBlank
        @Pattern(
            regexp = "^(Бакалавриат|Магистратура)$",
            message = "Некорректный уровень образования")
        String educationLevel) {}
