package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.grade_analysis.dto.GroupRequest;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/groups")
@PreAuthorize("hasRole('ADMIN')")
public class GroupController {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  public GroupController(GroupRepository groupRepository, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  @GetMapping
  public Page<GroupResponse> getAllGroups(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String course,
      @RequestParam(required = false) EducationLevel level,
      @RequestParam(required = false) String search) {

    PageRequest pageRequest = PageRequest.of(page - 1, 16, Sort.by("prefix", "course", "subgroup"));
    Page<Group> groupPage =
        groupRepository.findFiltered(
            (course != null && !course.isEmpty()) ? course : null,
            level,
            (search != null && !search.isEmpty()) ? search : null,
            pageRequest);

    return groupPage.map(this::mapToResponse);
  }

  @PostMapping
  public ResponseEntity<?> createGroup(@RequestBody @Valid GroupRequest request) {
    try {
      validateCourse(request);
      Group group = new Group();
      updateGroupFields(group, request);
      Group saved = groupRepository.save(group);
      return ResponseEntity.ok(mapToResponse(saved));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateGroup(
      @PathVariable Long id, @RequestBody @Valid GroupRequest request) {
    try {
      validateCourse(request);
      Group group =
          groupRepository
              .findById(id)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена"));
      updateGroupFields(group, request);
      Group saved = groupRepository.save(group);
      return ResponseEntity.ok(mapToResponse(saved));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
    if (!groupRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена");
    }
    groupRepository.deleteById(id);
    return ResponseEntity.ok(java.util.Map.of("message", "Группа удалена"));
  }

  @PostMapping("/{id}/students")
  public ResponseEntity<GroupResponse> assignStudents(
      @PathVariable Long id, @RequestBody List<Long> studentIds) {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена"));
    List<User> students = userRepository.findAllById(studentIds);
    group.getStudents().clear();
    group.getStudents().addAll(students);
    Group saved = groupRepository.save(group);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  private void validateCourse(GroupRequest request) {
    int courseNum = Integer.parseInt(request.course());
    if (request.educationLevel() == EducationLevel.MASTER && courseNum > 2) {
      throw new IllegalArgumentException("Для магистратуры доступны только 1 и 2 курсы");
    }
    if (courseNum < 1 || courseNum > 5) {
      throw new IllegalArgumentException("Курс должен быть от 1 до 5");
    }
  }

  private void updateGroupFields(Group group, GroupRequest request) {
    group.setPrefix(request.prefix());
    group.setCourse(request.course());
    group.setSubgroup(request.subgroup());
    group.setEducationLevel(request.educationLevel());
  }

  private GroupResponse mapToResponse(Group group) {
    List<GroupResponse.StudentShortDto> studentDtos =
        group.getStudents().stream()
            .map(s -> new GroupResponse.StudentShortDto(s.getId(), s.getFullName()))
            .collect(Collectors.toList());
    return new GroupResponse(
        group.getId(),
        group.getPrefix(),
        group.getCourse(),
        group.getSubgroup(),
        group.getEducationLevel(),
        group.getFullName(),
        studentDtos.size(),
        studentDtos);
  }
}
