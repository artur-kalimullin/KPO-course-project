package ru.ulstu.grade_analysis.dto;

import java.util.List;

public record StudentGradesResponse(
    String studentName,
    String groupName,
    List<GradeItem> grades,
    double average,
    List<String> debts) {

  public record GradeItem(String subjectName, Integer value) {}
}
