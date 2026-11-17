package ru.ulstu.grade_analysis.dto;

import java.util.List;

public record UserResponse(
    Long id,
    String fullName,
    String username,
    String course,
    String recordBookNumber,
    String educationForm,
    String educationLevel,
    List<String> groupNames,
    List<Long> groupIds) {}
