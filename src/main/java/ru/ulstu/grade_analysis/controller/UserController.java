package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.grade_analysis.dto.StudentCreateRequest;
import ru.ulstu.grade_analysis.dto.TeacherCreateRequest;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.RoleRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final GroupRepository groupRepository;
  private final PasswordEncoder passwordEncoder;

  public UserController(
      UserRepository userRepository,
      RoleRepository roleRepository,
      GroupRepository groupRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.groupRepository = groupRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/students")
  public List<UserResponse> getAllStudents() {
    return userRepository.findByRoleName("STUDENT").stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/teachers")
  public List<UserResponse> getAllTeachers() {
    return userRepository.findByRoleName("TEACHER").stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @PostMapping("/students")
  @Transactional
  public ResponseEntity<?> createStudent(@RequestBody @Valid StudentCreateRequest request) {
    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин уже занят");
    }
    Role role = roleRepository.findByName("STUDENT").orElseThrow();
    Group group = groupRepository.findById(request.groupId()).orElseThrow();

    User student = new User();
    student.setUsername(request.username());
    student.setPasswordHash(passwordEncoder.encode(request.password()));
    student.setLastName(request.lastName());
    student.setFirstName(request.firstName());
    student.setMiddleName(request.middleName());
    student.setRole(role);
    student.setCourse(request.course());
    student.setRecordBookNumber(request.recordBookNumber());
    student.setEducationForm(request.educationForm());
    student.setEducationLevel(request.educationLevel());

    // ФИКС: Обновляем обе стороны связи
    student.getStudentGroups().add(group);
    group.getStudents().add(student);

    userRepository.save(student);
    groupRepository.save(group);

    return ResponseEntity.ok(Map.of("message", "Студент добавлен"));
  }

  @PutMapping("/students/{id}")
  @Transactional
  public ResponseEntity<?> updateStudent(
      @PathVariable Long id, @RequestBody @Valid StudentCreateRequest request) {
    User student =
        userRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Студент не найден"));

    if (!student.getUsername().equals(request.username())
        && userRepository.findByUsername(request.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин уже занят");
    }

    student.setUsername(request.username());
    if (request.password() != null && !request.password().isEmpty()) {
      student.setPasswordHash(passwordEncoder.encode(request.password()));
    }
    student.setLastName(request.lastName());
    student.setFirstName(request.firstName());
    student.setMiddleName(request.middleName());
    student.setCourse(request.course());
    student.setRecordBookNumber(request.recordBookNumber());
    student.setEducationForm(request.educationForm());
    student.setEducationLevel(request.educationLevel());

    Group newGroup = groupRepository.findById(request.groupId()).orElseThrow();

    student.getStudentGroups().forEach(g -> g.getStudents().remove(student));
    student.getStudentGroups().clear();

    student.getStudentGroups().add(newGroup);
    newGroup.getStudents().add(student);

    userRepository.save(student);
    groupRepository.save(newGroup);

    return ResponseEntity.ok(Map.of("message", "Студент обновлен"));
  }

  @PostMapping("/teachers")
  public ResponseEntity<?> createTeacher(@RequestBody @Valid TeacherCreateRequest request) {
    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин уже занят");
    }
    Role role = roleRepository.findByName("TEACHER").orElseThrow();

    User teacher = new User();
    teacher.setUsername(request.username());
    teacher.setPasswordHash(passwordEncoder.encode(request.password()));
    teacher.setLastName(request.lastName());
    teacher.setFirstName(request.firstName());
    teacher.setMiddleName(request.middleName());
    teacher.setRole(role);

    if (request.groupIds() != null && !request.groupIds().isEmpty()) {
      List<Group> groups = groupRepository.findAllById(request.groupIds());
      groups.forEach(
          g -> {
            teacher.getTeacherGroups().add(g);
            g.getTeachers().add(teacher);
          });
    }

    userRepository.save(teacher);
    return ResponseEntity.ok(Map.of("message", "Преподаватель добавлен"));
  }

  @PutMapping("/teachers/{id}")
  @Transactional
  public ResponseEntity<?> updateTeacher(
      @PathVariable Long id, @RequestBody @Valid TeacherCreateRequest request) {
    User teacher =
        userRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Преподаватель не найден"));

    if (!teacher.getUsername().equals(request.username())
        && userRepository.findByUsername(request.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин уже занят");
    }

    teacher.setUsername(request.username());
    if (request.password() != null && !request.password().isEmpty()) {
      teacher.setPasswordHash(passwordEncoder.encode(request.password()));
    }
    teacher.setLastName(request.lastName());
    teacher.setFirstName(request.firstName());
    teacher.setMiddleName(request.middleName());

    teacher.getTeacherGroups().forEach(g -> g.getTeachers().remove(teacher));
    teacher.getTeacherGroups().clear();

    if (request.groupIds() != null && !request.groupIds().isEmpty()) {
      List<Group> groups = groupRepository.findAllById(request.groupIds());
      groups.forEach(
          g -> {
            teacher.getTeacherGroups().add(g);
            g.getTeachers().add(teacher);
          });
    }

    userRepository.save(teacher);
    return ResponseEntity.ok(Map.of("message", "Преподаватель обновлен"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    userRepository.delete(user);
    return ResponseEntity.ok(Map.of("message", "Пользователь удален"));
  }

  private UserResponse mapToResponse(User user) {
    java.util.List<String> groupNames = new java.util.ArrayList<>();
    java.util.List<Long> groupIds = new java.util.ArrayList<>();

    if (user.getRole().getName().equals("STUDENT")) {
      user.getStudentGroups()
          .forEach(
              g -> {
                groupNames.add(g.getFullName());
                groupIds.add(g.getId());
              });
    } else if (user.getRole().getName().equals("TEACHER")) {
      user.getTeacherGroups()
          .forEach(
              g -> {
                groupNames.add(g.getFullName());
                groupIds.add(g.getId());
              });
    }

    return new UserResponse(
        user.getId(),
        user.getFullName(),
        user.getUsername(),
        user.getCourse(),
        user.getRecordBookNumber(),
        user.getEducationForm(),
        user.getEducationLevel(),
        groupNames,
        groupIds);
  }

  public record UserResponse(
      Long id,
      String fullName,
      String username,
      String course,
      String recordBookNumber,
      String educationForm,
      String educationLevel,
      java.util.List<String> groupNames,
      java.util.List<Long> groupIds) {}
}
