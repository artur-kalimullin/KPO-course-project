package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
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
import ru.ulstu.grade_analysis.dto.GroupRequest;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.dto.MessageResponse;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;
import ru.ulstu.grade_analysis.service.GroupService;

@RestController
@RequestMapping("/api/admin/groups")
@PreAuthorize("hasRole('ADMIN')")
public class GroupController {

  private final GroupService groupService;

  public GroupController(GroupService groupService) {
    this.groupService = groupService;
  }

  @GetMapping
  public Page<GroupResponse> getAllGroups(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String course,
      @RequestParam(required = false) EducationLevel level,
      @RequestParam(required = false) String search) {

    return groupService.findAll(page, course, level, search);
  }

  @PostMapping
  public ResponseEntity<GroupResponse> createGroup(@RequestBody @Valid GroupRequest request) {
    return ResponseEntity.ok(groupService.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<GroupResponse> updateGroup(
      @PathVariable Long id, @RequestBody @Valid GroupRequest request) {
    return ResponseEntity.ok(groupService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<MessageResponse> deleteGroup(@PathVariable Long id) {
    groupService.delete(id);
    return ResponseEntity.ok(new MessageResponse("Группа удалена"));
  }

  @PostMapping("/{id}/students")
  public ResponseEntity<GroupResponse> assignStudents(
      @PathVariable Long id, @RequestBody List<Long> studentIds) {
    return ResponseEntity.ok(groupService.assignStudents(id, studentIds));
  }
}
