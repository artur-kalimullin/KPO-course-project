package ru.ulstu.grade_analysis.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.SubjectRequest;
import ru.ulstu.grade_analysis.dto.SubjectResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.mapper.SubjectMapper;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.BadRequestException;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class SubjectService {

  private static final int PAGE_SIZE = 16;

  private final SubjectRepository subjectRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final SubjectMapper subjectMapper;

  public SubjectService(
      SubjectRepository subjectRepository,
      UserRepository userRepository,
      GroupRepository groupRepository,
      SubjectMapper subjectMapper) {
    this.subjectRepository = subjectRepository;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.subjectMapper = subjectMapper;
  }

  @Transactional(readOnly = true)
  public Page<SubjectResponse> findAll(int page, String search, Long teacherId, Long groupId) {
    PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("name"));

    return subjectRepository
        .findFiltered(emptyToNull(search), teacherId, groupId, pageRequest)
        .map(subjectMapper::toResponse);
  }

  @Transactional
  public SubjectResponse create(SubjectRequest request) {
    if (subjectRepository.existsByName(request.name())) {
      throw new BadRequestException("Предмет с таким названием уже существует");
    }

    Subject subject = new Subject();
    subject.setName(request.name());
    updateRelations(subject, request);

    return subjectMapper.toResponse(subjectRepository.save(subject));
  }

  @Transactional
  public SubjectResponse update(Long id, SubjectRequest request) {
    if (subjectRepository.existsByNameAndIdNot(request.name(), id)) {
      throw new BadRequestException("Предмет с таким названием уже существует");
    }

    Subject subject =
        subjectRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Предмет не найден"));

    subject.setName(request.name());
    updateRelations(subject, request);

    return subjectMapper.toResponse(subjectRepository.save(subject));
  }

  @Transactional
  public void delete(Long id) {
    subjectRepository.deleteById(id);
  }

  private void updateRelations(Subject subject, SubjectRequest request) {
    subject.getTeachers().clear();
    if (request.teacherIds() != null && !request.teacherIds().isEmpty()) {
      List<User> teachers = userRepository.findAllById(request.teacherIds());
      subject.getTeachers().addAll(teachers);
    }

    subject.getGroups().clear();
    if (request.groupIds() != null && !request.groupIds().isEmpty()) {
      List<Group> groups = groupRepository.findAllById(request.groupIds());
      subject.getGroups().addAll(groups);
    }
  }

  private String emptyToNull(String value) {
    return value != null && !value.isEmpty() ? value : null;
  }
}
