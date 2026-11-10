package ru.ulstu.grade_analysis.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.dto.StudentResponse;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final SubjectRepository subjectRepository;

  public TeacherController(
      GroupRepository groupRepository,
      UserRepository userRepository,
      SubjectRepository subjectRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.subjectRepository = subjectRepository;
  }

  @GetMapping("/my-groups")
  public List<GroupResponse> getMyGroups(Authentication authentication) {
    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();
    return teacher.getTeacherGroups().stream()
        .map(
            g ->
                new GroupResponse(
                    g.getId(),
                    g.getPrefix(),
                    g.getCourse(),
                    g.getSubgroup(),
                    g.getEducationLevel(),
                    g.getFullName(),
                    g.getStudents().size(),
                    null))
        .collect(Collectors.toList());
  }

  @GetMapping("/groups/{groupId}/subjects")
  public List<Map<String, Object>> getSubjectsByGroup(
      @PathVariable Long groupId, Authentication authentication) {
    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();

    return subjectRepository.findAll().stream()
        .filter(s -> s.getTeachers().contains(teacher)) // Предмет ведет этот препод
        .filter(
            s ->
                s.getGroups().stream()
                    .anyMatch(g -> g.getId().equals(groupId))) // И у него есть эта группа
        .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
        .collect(Collectors.toList());
  }

  @GetMapping("/groups/{groupId}/students")
  public Page<StudentResponse> getStudentsByGroup(
      @PathVariable Long groupId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String search,
      Authentication authentication) {

    User teacher = userRepository.findByUsername(authentication.getName()).orElseThrow();

    boolean hasAccess =
        teacher.getTeacherGroups().stream().anyMatch(g -> g.getId().equals(groupId));
    if (!hasAccess) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.FORBIDDEN, "У вас нет доступа к этой группе");
    }

    PageRequest pageRequest = PageRequest.of(page - 1, 16, Sort.by("lastName", "firstName"));
    Page<User> students =
        userRepository.findStudentsInGroup(
            groupId, (search != null && !search.isEmpty()) ? search : null, pageRequest);

    return students.map(
        s ->
            new StudentResponse(
                s.getId(),
                s.getFullName(),
                s.getUsername(),
                s.getCourse(),
                s.getRecordBookNumber()));
  }
}
