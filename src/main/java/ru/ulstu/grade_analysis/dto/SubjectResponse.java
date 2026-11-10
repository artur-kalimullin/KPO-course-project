package ru.ulstu.grade_analysis.dto;

import java.util.List;

public record SubjectResponse(
    Long id,
    String name,
    Integer teachersCount,
    List<TeacherShortDto> teachers,
    List<GroupShortDto> groups) {
  public record TeacherShortDto(Long id, String fullName) {}

  public record GroupShortDto(Long id, String fullName) {}
}
