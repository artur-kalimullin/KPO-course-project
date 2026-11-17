package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
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
import ru.ulstu.grade_analysis.dto.MessageResponse;
import ru.ulstu.grade_analysis.dto.StudentCreateRequest;
import ru.ulstu.grade_analysis.dto.TeacherCreateRequest;
import ru.ulstu.grade_analysis.dto.UserResponse;
import ru.ulstu.grade_analysis.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/students")
  public Page<UserResponse> getAllStudents(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String course,
      @RequestParam(required = false) String level) {

    return userService.findStudents(page, search, course, level);
  }

  @GetMapping("/teachers")
  public Page<UserResponse> getAllTeachers(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Long groupId) {

    return userService.findTeachers(page, search, groupId);
  }

  @PostMapping("/students")
  public ResponseEntity<MessageResponse> createStudent(
      @RequestBody @Valid StudentCreateRequest request) {
    userService.createStudent(request);
    return ResponseEntity.ok(new MessageResponse("Студент добавлен"));
  }

  @PutMapping("/students/{id}")
  public ResponseEntity<MessageResponse> updateStudent(
      @PathVariable Long id, @RequestBody @Valid StudentCreateRequest request) {
    userService.updateStudent(id, request);
    return ResponseEntity.ok(new MessageResponse("Студент обновлен"));
  }

  @PostMapping("/teachers")
  public ResponseEntity<MessageResponse> createTeacher(
      @RequestBody @Valid TeacherCreateRequest request) {
    userService.createTeacher(request);
    return ResponseEntity.ok(new MessageResponse("Преподаватель добавлен"));
  }

  @PutMapping("/teachers/{id}")
  public ResponseEntity<MessageResponse> updateTeacher(
      @PathVariable Long id, @RequestBody @Valid TeacherCreateRequest request) {
    userService.updateTeacher(id, request);
    return ResponseEntity.ok(new MessageResponse("Преподаватель обновлен"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.ok(new MessageResponse("Пользователь удален"));
  }
}
