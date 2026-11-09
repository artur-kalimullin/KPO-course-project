package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentCreateRequest(
    @NotBlank @Size(min = 6, max = 40) String username,
    @NotBlank @Size(min = 6, max = 40) String password,
    @NotBlank @Size(max = 60) String firstName,
    @NotBlank @Size(max = 60) String lastName,
    @Size(max = 60) String middleName,
    @NotBlank @Size(max = 1) String course,
    @NotNull Long groupId,
    @NotBlank @Pattern(regexp = "\\d{6}", message = "Номер зачетки должен состоять из 6 цифр")
        String recordBookNumber,
    @NotBlank String educationForm,
    @NotBlank String educationLevel) {}
