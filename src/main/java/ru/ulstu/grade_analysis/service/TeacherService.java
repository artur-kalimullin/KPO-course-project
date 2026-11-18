package ru.ulstu.grade_analysis.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.dto.StudentResponse;
import ru.ulstu.grade_analysis.dto.SubjectOption;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.mapper.GroupMapper;
import ru.ulstu.grade_analysis.mapper.SubjectMapper;
import ru.ulstu.grade_analysis.mapper.UserMapper;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.ForbiddenException;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class TeacherService {

  private static final int PAGE_SIZE = 16;

  private final UserRepository userRepository;
  private final SubjectRepository subjectRepository;
  private final GroupMapper groupMapper;
  private final SubjectMapper subjectMapper;
  private final UserMapper userMapper;

  public TeacherService(
      UserRepository userRepository,
      SubjectRepository subjectRepository,
      GroupMapper groupMapper,
      SubjectMapper subjectMapper,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.subjectRepository = subjectRepository;
    this.groupMapper = groupMapper;
    this.subjectMapper = subjectMapper;
    this.userMapper = userMapper;
  }

  @Transactional(readOnly = true)
  public List<GroupResponse> findMyGroups(String username) {
    User teacher = findTeacher(username);

    return teacher.getTeacherGroups().stream()
        .sorted(Comparator.comparing(Group::getFullName))
        .map(groupMapper::toSummary)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<SubjectOption> findSubjectsByGroup(String username, Long groupId) {
    User teacher = findTeacher(username);

    return subjectRepository.findAll().stream()
        .filter(subject -> subject.getTeachers().contains(teacher))
        .filter(subject -> isSubjectForGroup(subject, groupId))
        .map(subjectMapper::toOption)
        .toList();
  }

  @Transactional(readOnly = true)
  public Page<StudentResponse> findStudentsByGroup(
      String username, Long groupId, int page, String search) {
    User teacher = findTeacher(username);
    requireGroupAccess(teacher, groupId);

    PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("lastName", "firstName"));

    return userRepository
        .findStudentsInGroup(groupId, emptyToNull(search), pageRequest)
        .map(userMapper::toStudentResponse);
  }

  private boolean isSubjectForGroup(Subject subject, Long groupId) {
    return subject.getGroups().stream().anyMatch(group -> group.getId().equals(groupId));
  }

  private void requireGroupAccess(User teacher, Long groupId) {
    boolean hasAccess =
        teacher.getTeacherGroups().stream().anyMatch(group -> group.getId().equals(groupId));
    if (!hasAccess) {
      throw new ForbiddenException("У вас нет доступа к этой группе");
    }
  }

  private User findTeacher(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException("Преподаватель не найден"));
  }

  private String emptyToNull(String value) {
    return value != null && !value.isEmpty() ? value : null;
  }
}
