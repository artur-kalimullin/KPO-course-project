package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.GradeDto.GradeRequest;
import ru.ulstu.grade_analysis.dto.GradeDto.GradeResponse;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/teacher/grades")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherGradeController {

  private final GradeRepository gradeRepository;
  private final SubjectRepository subjectRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public TeacherGradeController(
      GradeRepository gradeRepository,
      SubjectRepository subjectRepository,
      UserRepository userRepository,
      GroupRepository groupRepository) {
    this.gradeRepository = gradeRepository;
    this.subjectRepository = subjectRepository;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
  }

  @GetMapping("/my-subjects")
  public List<Map<String, Object>> getMySubjects(Authentication authentication) {
    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();
    return subjectRepository.findAll().stream()
        .filter(s -> s.getTeachers().contains(teacher))
        .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
        .collect(Collectors.toList());
  }

  @GetMapping("/subject/{subjectId}")
  public List<GradeResponse> getGradesBySubject(
      @PathVariable Long subjectId, Authentication authentication) {
    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();
    return gradeRepository.findBySubjectIdAndTeacherId(subjectId, teacher.getId()).stream()
        .map(
            g ->
                new GradeResponse(
                    g.getId(),
                    g.getStudent().getId(),
                    g.getStudent().getFullName(),
                    g.getSubject().getId(),
                    g.getSubject().getName(),
                    null,
                    null,
                    g.getValue(),
                    g.getDate()))
        .collect(Collectors.toList());
  }

  @GetMapping("/subject/{subjectId}/details")
  public Map<String, Object> getSubjectDetails(
      @PathVariable Long subjectId, Authentication authentication) {
    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();

    Subject subject = subjectRepository.findById(subjectId).orElseThrow();
    if (!subject.getTeachers().contains(teacher)) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.FORBIDDEN, "У вас нет доступа к этому предмету");
    }

    List<Map<String, Object>> groupsData =
        subject.getGroups().stream()
            .sorted(java.util.Comparator.comparing(Group::getFullName))
            .map(
                g -> {
                  List<Map<String, Object>> studentsData =
                      g.getStudents().stream()
                          .sorted(java.util.Comparator.comparing(User::getFullName))
                          .map(
                              s ->
                                  Map.<String, Object>of(
                                      "id", s.getId(), "fullName", s.getFullName()))
                          .collect(Collectors.toList());

                  return Map.<String, Object>of(
                      "id", g.getId(),
                      "fullName", g.getFullName(),
                      "students", studentsData);
                })
            .collect(Collectors.toList());

    return Map.of("groups", groupsData);
  }

  @PostMapping
  public ResponseEntity<?> saveGrade(
      @RequestBody @Valid GradeRequest request, Authentication authentication) {
    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();

    Grade grade =
        gradeRepository
            .findByStudentIdAndSubjectIdAndTeacherId(
                request.studentId(), request.subjectId(), teacher.getId())
            .orElse(new Grade());

    grade.setStudent(userRepository.findById(request.studentId()).orElseThrow());
    grade.setSubject(subjectRepository.findById(request.subjectId()).orElseThrow());
    grade.setTeacher(teacher);
    grade.setValue(request.value());
    grade.setDate(LocalDate.now());

    gradeRepository.save(grade);
    return ResponseEntity.ok(Map.of("message", "Оценка сохранена"));
  }
}
