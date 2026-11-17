package ru.ulstu.grade_analysis.dto;

import java.util.List;

public record RatingResponse(
    String groupName,
    List<RatingEntry> top3,
    int myPlace,
    double myAverage,
    int totalStudents,
    long studentsWithDebts,
    long studentsWithoutDebts) {

  public record RatingEntry(Long id, String fullName, double average, boolean hasDebts) {}
}
