package ru.ulstu.grade_analysis.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.GradeDto.GradeRequest;
import ru.ulstu.grade_analysis.dto.GradeDto.GradeResponse;
import ru.ulstu.grade_analysis.dto.SubjectDetailsResponse;
import ru.ulstu.grade_analysis.dto.SubjectOption;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.mapper.GradeMapper;
import ru.ulstu.grade_analysis.mapper.SubjectMapper;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.ForbiddenException;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class GradeService {

  private final GradeRepository gradeRepository;
  private final SubjectRepository subjectRepository;
  private final UserRepository userRepository;
  private final SubjectMapper subjectMapper;
  private final GradeMapper gradeMapper;

  public GradeService(
      GradeRepository gradeRepository,
      SubjectRepository subjectRepository,
      UserRepository userRepository,
      SubjectMapper subjectMapper,
      GradeMapper gradeMapper) {
    this.gradeRepository = gradeRepository;
    this.subjectRepository = subjectRepository;
    this.userRepository = userRepository;
    this.subjectMapper = subjectMapper;
    this.gradeMapper = gradeMapper;
  }

  @Transactional(readOnly = true)
  public List<SubjectOption> findMySubjects(String username) {
    User teacher = findTeacher(username);

    return subjectRepository.findAll().stream()
        .filter(subject -> subject.getTeachers().contains(teacher))
        .map(subjectMapper::toOption)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<GradeResponse> findGradesBySubject(String username, Long subjectId) {
    User teacher = findTeacher(username);

    return gradeRepository.findBySubjectIdAndTeacherId(subjectId, teacher.getId()).stream()
        .map(gradeMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public SubjectDetailsResponse getSubjectDetails(String username, Long subjectId) {
    User teacher = findTeacher(username);
    Subject subject = findSubject(subjectId);

    if (!subject.getTeachers().contains(teacher)) {
      throw new ForbiddenException("У вас нет доступа к этому предмету");
    }

    List<SubjectDetailsResponse.GroupDetails> groups =
        subject.getGroups().stream()
            .sorted(Comparator.comparing(Group::getFullName))
            .map(this::toGroupDetails)
            .toList();

    return new SubjectDetailsResponse(groups);
  }

  @Transactional
  public void saveGrade(String username, GradeRequest request) {
    User teacher = findTeacher(username);

    Grade grade =
        gradeRepository
            .findByStudentIdAndSubjectIdAndTeacherId(
                request.studentId(), request.subjectId(), teacher.getId())
            .orElseGet(Grade::new);

    grade.setStudent(findUser(request.studentId()));
    grade.setSubject(findSubject(request.subjectId()));
    grade.setTeacher(teacher);
    grade.setValue(request.value());
    grade.setDate(LocalDate.now());

    gradeRepository.save(grade);
  }

  private SubjectDetailsResponse.GroupDetails toGroupDetails(Group group) {
    List<SubjectDetailsResponse.StudentShort> students =
        group.getStudents().stream()
            .sorted(Comparator.comparing(User::getFullName))
            .map(
                student ->
                    new SubjectDetailsResponse.StudentShort(student.getId(), student.getFullName()))
            .toList();

    return new SubjectDetailsResponse.GroupDetails(group.getId(), group.getFullName(), students);
  }

  private User findTeacher(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException("Преподаватель не найден"));
  }

  private User findUser(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
  }

  private Subject findSubject(Long id) {
    return subjectRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Предмет не найден"));
  }
}
