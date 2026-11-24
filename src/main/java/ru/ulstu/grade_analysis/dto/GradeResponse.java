package ru.ulstu.grade_analysis.dto;

public record GradeResponse(
    Long id,
    Long studentId,
    String studentFullName,
    Long subjectId,
    String subjectName,
    Long groupId,
    String groupName,
    Integer value) {}
