package ru.ulstu.grade_analysis.dto;

import java.util.List;

public record GroupResponse(
    Long id, String name, Integer studentsCount, List<StudentShortDto> students) {
  public record StudentShortDto(Long id, String fullName) {}
}
