package ru.ulstu.grade_analysis.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.StudentGradesResponse;
import ru.ulstu.grade_analysis.service.StudentService;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

  private final StudentService studentService;

  public StudentController(StudentService studentService) {
    this.studentService = studentService;
  }

  @GetMapping("/my-grades")
  public StudentGradesResponse getMyGrades(Authentication authentication) {
    return studentService.getMyGrades(authentication.getName());
  }
}
