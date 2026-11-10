package ru.ulstu.grade_analysis.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/teacher/stats")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherStatsController {

  private final GroupRepository groupRepository;
  private final SubjectRepository subjectRepository;
  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;

  public TeacherStatsController(
      GroupRepository groupRepository,
      SubjectRepository subjectRepository,
      UserRepository userRepository,
      GradeRepository gradeRepository) {
    this.groupRepository = groupRepository;
    this.subjectRepository = subjectRepository;
    this.userRepository = userRepository;
    this.gradeRepository = gradeRepository;
  }

  @GetMapping
  public Map<String, Object> getStats(
      @RequestParam Long groupId, @RequestParam Long subjectId, Authentication authentication) {

    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();
    Group group = groupRepository.findById(groupId).orElseThrow();
    Subject subject = subjectRepository.findById(subjectId).orElseThrow();

    List<User> allStudents = new ArrayList<>(group.getStudents());
    int totalStudents = allStudents.size();

    List<Grade> grades = gradeRepository.findBySubjectIdAndTeacherId(subjectId, teacher.getId());

    Set<Long> studentIds = allStudents.stream().map(User::getId).collect(Collectors.toSet());
    List<Grade> groupGrades =
        grades.stream()
            .filter(g -> studentIds.contains(g.getStudent().getId()))
            .collect(Collectors.toList());

    int certifiedCount = 0;
    int failedCount = 0;

    Map<Integer, Integer> gradeDistribution = new HashMap<>();
    gradeDistribution.put(5, 0);
    gradeDistribution.put(4, 0);
    gradeDistribution.put(3, 0);
    gradeDistribution.put(2, 0);
    gradeDistribution.put(0, 0); // 0 = Неявка

    List<Map<String, Object>> failedStudents = new ArrayList<>();

    for (User student : allStudents) {
      Optional<Grade> gradeOpt =
          groupGrades.stream()
              .filter(g -> g.getStudent().getId().equals(student.getId()))
              .findFirst();

      if (gradeOpt.isPresent()) {
        int val = gradeOpt.get().getValue();

        gradeDistribution.put(val, gradeDistribution.getOrDefault(val, 0) + 1);

        if (val >= 3) {
          certifiedCount++;
        } else {
          failedCount++;
          failedStudents.add(Map.of("fullName", student.getFullName(), "grade", val));
        }
      } else {
        failedCount++;
        failedStudents.add(Map.of("fullName", student.getFullName(), "grade", -1));
      }
    }

    return Map.of(
        "groupName", group.getFullName(),
        "totalStudents", totalStudents,
        "certifiedCount", certifiedCount,
        "failedCount", failedCount,
        "gradeDistribution", gradeDistribution,
        "failedStudents", failedStudents);
  }
}
