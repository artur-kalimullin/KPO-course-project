package ru.ulstu.grade_analysis.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/students")
  public List<UserResponse> getAllStudents() {
    return userRepository.findByRoleName("STUDENT").stream()
        .map(u -> new UserResponse(u.getId(), u.getFullName()))
        .collect(Collectors.toList());
  }

  @GetMapping("/teachers")
  public List<UserResponse> getAllTeachers() {
    return userRepository.findByRoleName("TEACHER").stream()
        .map(u -> new UserResponse(u.getId(), u.getFullName()))
        .collect(Collectors.toList());
  }

  public record UserResponse(Long id, String fullName) {}
}
