package ru.ulstu.grade_analysis.dto;

import java.util.List;

public record SubjectDetailsResponse(List<GroupDetails> groups) {

  public record GroupDetails(Long id, String fullName, List<StudentShort> students) {}

  public record StudentShort(Long id, String fullName) {}
}
