package ru.ulstu.grade_analysis.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
@RequestMapping("/api/student/rating")
@PreAuthorize("hasRole('STUDENT')")
public class StudentRatingController {

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;

  public StudentRatingController(UserRepository userRepository, GradeRepository gradeRepository) {
    this.userRepository = userRepository;
    this.gradeRepository = gradeRepository;
  }

  @GetMapping
  public Map<String, Object> getGroupRating(Authentication authentication) {
    User currentStudent = userRepository.findByUsername(authentication.getName()).orElseThrow();

    Group group =
        currentStudent.getStudentGroups().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Студент не привязан к группе"));

    List<Map<String, Object>> ranking = new ArrayList<>();

    for (User student : group.getStudents()) {
      List<Grade> grades = gradeRepository.findByStudentId(student.getId());

      double sum = 0;
      int count = 0;
      boolean hasDebts = false;

      for (Grade g : grades) {
        if (g.getValue() >= 2 && g.getValue() <= 5) {
          sum += g.getValue();
          count++;
        }
        if (g.getValue() == 2 || g.getValue() == 0) {
          hasDebts = true;
        }
      }

      double average = count > 0 ? Math.round((sum / count) * 100.0) / 100.0 : 0.0;

      ranking.add(
          Map.of(
              "id",
              student.getId(),
              "fullName",
              student.getFullName(),
              "average",
              average,
              "hasDebts",
              hasDebts));
    }

    ranking.sort((a, b) -> Double.compare((Double) b.get("average"), (Double) a.get("average")));

    int myPlace = 1;
    for (int i = 0; i < ranking.size(); i++) {
      if (ranking.get(i).get("id").equals(currentStudent.getId())) {
        myPlace = i + 1;
        break;
      }
    }

    List<Map<String, Object>> top3 = ranking.stream().limit(3).collect(Collectors.toList());

    long studentsWithDebts = ranking.stream().filter(r -> (Boolean) r.get("hasDebts")).count();
    long studentsWithoutDebts = ranking.size() - studentsWithDebts;

    return Map.of(
        "groupName", group.getFullName(),
        "top3", top3,
        "myPlace", myPlace,
        "myAverage",
            ranking.stream()
                .filter(r -> r.get("id").equals(currentStudent.getId()))
                .findFirst()
                .get()
                .get("average"),
        "totalStudents", ranking.size(),
        "studentsWithDebts", studentsWithDebts,
        "studentsWithoutDebts", studentsWithoutDebts);
  }
}
