package ru.ulstu.grade_analysis.dto;

public record StudentResponse(
    Long id, String fullName, String username, String course, String recordBookNumber) {}
