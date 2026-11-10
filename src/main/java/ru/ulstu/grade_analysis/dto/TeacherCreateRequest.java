package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TeacherCreateRequest(
    @NotBlank @Size(min = 6, max = 40) String username,
    String password,
    @NotBlank @Size(max = 60) String firstName,
    @NotBlank @Size(max = 60) String lastName,
    @Size(max = 60) String middleName,
    List<Long> groupIds) {}
