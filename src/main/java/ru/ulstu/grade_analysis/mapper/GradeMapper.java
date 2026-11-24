package ru.ulstu.grade_analysis.mapper;

import org.springframework.stereotype.Component;
import ru.ulstu.grade_analysis.dto.GradeResponse;
import ru.ulstu.grade_analysis.entity.Grade;

@Component
public class GradeMapper {

  public GradeResponse toResponse(Grade grade) {
    return new GradeResponse(
        grade.getId(),
        grade.getStudent().getId(),
        grade.getStudent().getFullName(),
        grade.getSubject().getId(),
        grade.getSubject().getName(),
        null,
        null,
        grade.getValue());
  }
}
