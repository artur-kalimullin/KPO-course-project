package ru.ulstu.grade_analysis.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.GroupRequest;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.mapper.GroupMapper;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.BadRequestException;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class GroupService {

  private static final int PAGE_SIZE = 16;

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final GroupMapper groupMapper;

  public GroupService(
      GroupRepository groupRepository, UserRepository userRepository, GroupMapper groupMapper) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
    this.groupMapper = groupMapper;
  }

  @Transactional(readOnly = true)
  public Page<GroupResponse> findAll(int page, String course, EducationLevel level, String search) {
    PageRequest pageRequest =
        PageRequest.of(page - 1, PAGE_SIZE, Sort.by("prefix", "course", "subgroup"));

    return groupRepository
        .findFiltered(emptyToNull(course), level, emptyToNull(search), pageRequest)
        .map(groupMapper::toResponse);
  }

  @Transactional
  public GroupResponse create(GroupRequest request) {
    validateCourse(request);

    Group group = new Group();
    apply(group, request);

    return groupMapper.toResponse(groupRepository.save(group));
  }

  @Transactional
  public GroupResponse update(Long id, GroupRequest request) {
    validateCourse(request);

    Group group = findGroup(id);
    apply(group, request);

    return groupMapper.toResponse(groupRepository.save(group));
  }

  @Transactional
  public void delete(Long id) {
    if (!groupRepository.existsById(id)) {
      throw new NotFoundException("Группа не найдена");
    }
    groupRepository.deleteById(id);
  }

  @Transactional
  public GroupResponse assignStudents(Long id, List<Long> studentIds) {
    Group group = findGroup(id);
    List<User> students = userRepository.findAllById(studentIds);

    group.getStudents().clear();
    group.getStudents().addAll(students);

    return groupMapper.toResponse(groupRepository.save(group));
  }

  private Group findGroup(Long id) {
    return groupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Группа не найдена"));
  }

  private void validateCourse(GroupRequest request) {
    int courseNumber;
    try {
      courseNumber = Integer.parseInt(request.course());
    } catch (NumberFormatException e) {
      throw new BadRequestException("Курс должен быть числом от 1 до 5");
    }

    if (request.educationLevel() == EducationLevel.MASTER && courseNumber > 2) {
      throw new BadRequestException("Для магистратуры доступны только 1 и 2 курсы");
    }
    if (courseNumber < 1 || courseNumber > 5) {
      throw new BadRequestException("Курс должен быть от 1 до 5");
    }
  }

  private void apply(Group group, GroupRequest request) {
    group.setPrefix(request.prefix());
    group.setCourse(request.course());
    group.setSubgroup(request.subgroup());
    group.setEducationLevel(request.educationLevel());
  }

  private String emptyToNull(String value) {
    return value != null && !value.isEmpty() ? value : null;
  }
}
