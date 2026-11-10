package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class GradeDto {
  public record GradeRequest(
      @NotNull Long studentId, @NotNull Long subjectId, @NotNull Integer value) {}

  public record GradeResponse(
      Long id,
      Long studentId,
      String studentFullName,
      Long subjectId,
      String subjectName,
      Long groupId,
      String groupName,
      Integer value,
      LocalDate date) {}
}
