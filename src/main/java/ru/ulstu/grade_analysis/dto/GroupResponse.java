package ru.ulstu.grade_analysis.dto;

import java.util.List;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;

public record GroupResponse(
    Long id,
    String prefix,
    String course,
    String subgroup,
    EducationLevel educationLevel,
    String fullName,
    Integer studentsCount,
    List<StudentShortDto> students) {
  public record StudentShortDto(Long id, String fullName) {}
}
