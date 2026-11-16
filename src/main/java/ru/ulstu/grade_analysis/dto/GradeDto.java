package ru.ulstu.grade_analysis.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class GradeDto {
  public record GradeRequest(
      @NotNull Long studentId,
      @NotNull Long subjectId,
      @NotNull
          @Min(value = 0, message = "Оценка должна быть от 0 до 5")
          @Max(value = 5, message = "Оценка должна быть от 0 до 5")
          Integer value) {}

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
