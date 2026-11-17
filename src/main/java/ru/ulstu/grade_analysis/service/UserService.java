package ru.ulstu.grade_analysis.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.StudentCreateRequest;
import ru.ulstu.grade_analysis.dto.TeacherCreateRequest;
import ru.ulstu.grade_analysis.dto.UserResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.mapper.UserMapper;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.RoleRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.BadRequestException;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class UserService {

  private static final int PAGE_SIZE = 16;

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final GroupRepository groupRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  public UserService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      GroupRepository groupRepository,
      PasswordEncoder passwordEncoder,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.groupRepository = groupRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
  }

  @Transactional(readOnly = true)
  public Page<UserResponse> findStudents(int page, String search, String course, String level) {
    PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("lastName", "firstName"));

    return userRepository
        .findFilteredStudents(
            emptyToNull(search), emptyToNull(course), emptyToNull(level), pageRequest)
        .map(userMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<UserResponse> findTeachers(int page, String search, Long groupId) {
    PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("lastName", "firstName"));

    return userRepository
        .findFilteredTeachers(emptyToNull(search), groupId, pageRequest)
        .map(userMapper::toResponse);
  }

  @Transactional
  public void createStudent(StudentCreateRequest request) {
    requirePassword(request.password());
    requireUniqueUsername(request.username(), null);

    Role role = findRole("STUDENT");
    Group group = findGroup(request.groupId());

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

    student.getStudentGroups().add(group);
    group.getStudents().add(student);

    userRepository.save(student);
    groupRepository.save(group);
  }

  @Transactional
  public void updateStudent(Long id, StudentCreateRequest request) {
    User student =
        userRepository.findById(id).orElseThrow(() -> new NotFoundException("Студент не найден"));

    requireUniqueUsername(request.username(), student.getUsername());

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

    Group newGroup = findGroup(request.groupId());

    student.getStudentGroups().forEach(group -> group.getStudents().remove(student));
    student.getStudentGroups().clear();

    student.getStudentGroups().add(newGroup);
    newGroup.getStudents().add(student);

    userRepository.save(student);
    groupRepository.save(newGroup);
  }

  @Transactional
  public void createTeacher(TeacherCreateRequest request) {
    requirePassword(request.password());
    requireUniqueUsername(request.username(), null);

    Role role = findRole("TEACHER");

    User teacher = new User();
    teacher.setUsername(request.username());
    teacher.setPasswordHash(passwordEncoder.encode(request.password()));
    teacher.setLastName(request.lastName());
    teacher.setFirstName(request.firstName());
    teacher.setMiddleName(request.middleName());
    teacher.setRole(role);

    assignGroups(teacher, request.groupIds());

    userRepository.save(teacher);
  }

  @Transactional
  public void updateTeacher(Long id, TeacherCreateRequest request) {
    User teacher =
        userRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Преподаватель не найден"));

    requireUniqueUsername(request.username(), teacher.getUsername());

    teacher.setUsername(request.username());
    if (request.password() != null && !request.password().isEmpty()) {
      teacher.setPasswordHash(passwordEncoder.encode(request.password()));
    }
    teacher.setLastName(request.lastName());
    teacher.setFirstName(request.firstName());
    teacher.setMiddleName(request.middleName());

    teacher.getTeacherGroups().forEach(group -> group.getTeachers().remove(teacher));
    teacher.getTeacherGroups().clear();

    assignGroups(teacher, request.groupIds());

    userRepository.save(teacher);
  }

  @Transactional
  public void delete(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

    userRepository.delete(user);
  }

  private void assignGroups(User teacher, List<Long> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      return;
    }

    List<Group> groups = groupRepository.findAllById(groupIds);
    groups.forEach(
        group -> {
          teacher.getTeacherGroups().add(group);
          group.getTeachers().add(teacher);
        });
  }

  private void requirePassword(String password) {
    if (password == null || password.trim().isEmpty()) {
      throw new BadRequestException("Пароль не может быть пустым");
    }
  }

  private void requireUniqueUsername(String username, String currentUsername) {
    if (username.equals(currentUsername)) {
      return;
    }
    if (userRepository.findByUsername(username).isPresent()) {
      throw new BadRequestException("Логин уже занят");
    }
  }

  private Role findRole(String name) {
    return roleRepository
        .findByName(name)
        .orElseThrow(() -> new NotFoundException("Роль " + name + " не найдена"));
  }

  private Group findGroup(Long id) {
    return groupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Группа не найдена"));
  }

  private String emptyToNull(String value) {
    return value != null && !value.isEmpty() ? value : null;
  }
}
