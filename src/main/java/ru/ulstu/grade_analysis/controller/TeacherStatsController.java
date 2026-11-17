package ru.ulstu.grade_analysis.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.TeacherStatsResponse;
import ru.ulstu.grade_analysis.service.TeacherStatsService;

@RestController
@RequestMapping("/api/teacher/stats")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherStatsController {

  private final TeacherStatsService teacherStatsService;

  public TeacherStatsController(TeacherStatsService teacherStatsService) {
    this.teacherStatsService = teacherStatsService;
  }

  @GetMapping
  public TeacherStatsResponse getStats(
      @RequestParam Long groupId, @RequestParam Long subjectId, Authentication authentication) {
    return teacherStatsService.getStats(authentication.getName(), groupId, subjectId);
  }
}
