package ru.ulstu.grade_analysis.dto;

import java.util.List;
import java.util.Map;

public record TeacherStatsResponse(
    String groupName,
    int totalStudents,
    int certifiedCount,
    int failedCount,
    Map<Integer, Integer> gradeDistribution,
    List<FailedStudent> failedStudents) {

  public record FailedStudent(String fullName, int grade) {}
}
