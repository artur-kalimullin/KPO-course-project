package ru.ulstu.grade_analysis.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;

  public StudentController(UserRepository userRepository, GradeRepository gradeRepository) {
    this.userRepository = userRepository;
    this.gradeRepository = gradeRepository;
  }

  @GetMapping("/my-grades")
  public ResponseEntity<?> getMyGrades(Authentication authentication) {
    User student = userRepository.findByUsername(authentication.getName()).orElseThrow();
    List<Grade> grades = gradeRepository.findByStudentId(student.getId());

    String groupName =
        student.getStudentGroups().stream()
            .map(Group::getFullName)
            .findFirst()
            .orElse("Без группы");

    List<Map<String, Object>> gradesList =
        grades.stream()
            .map(
                g ->
                    Map.<String, Object>of(
                        "subjectName", g.getSubject().getName(),
                        "value", g.getValue()))
            .collect(Collectors.toList());

    double sum = 0;
    int count = 0;
    for (Grade g : grades) {
      if (g.getValue() >= 2 && g.getValue() <= 5) {
        sum += g.getValue();
        count++;
      }
    }
    double average = count > 0 ? Math.round((sum / count) * 100.0) / 100.0 : 0.0;

    List<String> debts =
        grades.stream()
            .filter(g -> g.getValue() == 2 || g.getValue() == 0)
            .map(g -> g.getSubject().getName())
            .collect(Collectors.toList());

    return ResponseEntity.ok(
        Map.of(
            "studentName", student.getFullName(),
            "groupName", groupName,
            "grades", gradesList,
            "average", average,
            "debts", debts));
  }
}
