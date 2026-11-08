package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.grade_analysis.dto.GroupRequest;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.entity.Group;
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
  public List<GroupResponse> getAllGroups() {
    return groupRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<GroupResponse> createGroup(@RequestBody @Valid GroupRequest request) {
    if (groupRepository.existsByName(request.name())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Группа с таким названием уже существует");
    }
    Group group = new Group();
    group.setName(request.name());
    Group saved = groupRepository.save(group);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  @PutMapping("/{id}")
  public ResponseEntity<GroupResponse> updateGroup(
      @PathVariable Long id, @RequestBody @Valid GroupRequest request) {
    if (groupRepository.existsByNameAndIdNot(request.name(), id)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Группа с таким названием уже существует");
    }
    Group group = groupRepository.findById(id).orElseThrow();
    group.setName(request.name());
    Group saved = groupRepository.save(group);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
    groupRepository.deleteById(id);
    return ResponseEntity.ok(Map.of("message", "Группа удалена"));
  }

  @PostMapping("/{id}/students")
  public ResponseEntity<GroupResponse> assignStudents(
      @PathVariable Long id, @RequestBody List<Long> studentIds) {
    Group group = groupRepository.findById(id).orElseThrow();
    List<User> students = userRepository.findAllById(studentIds);
    group.getStudents().clear();
    group.getStudents().addAll(students);
    Group saved = groupRepository.save(group);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  private GroupResponse mapToResponse(Group group) {
    List<GroupResponse.StudentShortDto> studentDtos =
        group.getStudents().stream()
            .map(s -> new GroupResponse.StudentShortDto(s.getId(), s.getFullName()))
            .collect(Collectors.toList());
    return new GroupResponse(group.getId(), group.getName(), studentDtos.size(), studentDtos);
  }
}
