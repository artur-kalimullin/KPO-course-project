package ru.ulstu.grade_analysis.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.TeacherStatsResponse;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.ForbiddenException;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class TeacherStatsService {

  private final GradeRepository gradeRepository;
  private final GroupRepository groupRepository;
  private final SubjectRepository subjectRepository;
  private final UserRepository userRepository;

  public TeacherStatsService(
      GradeRepository gradeRepository,
      GroupRepository groupRepository,
      SubjectRepository subjectRepository,
      UserRepository userRepository) {
    this.gradeRepository = gradeRepository;
    this.groupRepository = groupRepository;
    this.subjectRepository = subjectRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public TeacherStatsResponse getStats(String username, Long groupId, Long subjectId) {
    User teacher =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Преподаватель не найден"));

    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new NotFoundException("Группа не найдена"));
    Subject subject =
        subjectRepository
            .findById(subjectId)
            .orElseThrow(() -> new NotFoundException("Предмет не найден"));

    requireAccess(teacher, group, subject);

    List<User> allStudents = new ArrayList<>(group.getStudents());
    List<Grade> groupGrades = findGroupGrades(subjectId, teacher, allStudents);

    Map<Integer, Integer> distribution = emptyDistribution();
    List<TeacherStatsResponse.FailedStudent> failedStudents = new ArrayList<>();
    int certifiedCount = 0;
    int failedCount = 0;

    for (User student : allStudents) {
      Optional<Grade> grade = findStudentGrade(groupGrades, student);

      if (grade.isPresent()) {
        int value = grade.get().getValue();
        distribution.merge(value, 1, Integer::sum);

        if (value >= 3) {
          certifiedCount++;
        } else {
          failedCount++;
          failedStudents.add(new TeacherStatsResponse.FailedStudent(student.getFullName(), value));
        }
      } else {
        failedCount++;
        failedStudents.add(new TeacherStatsResponse.FailedStudent(student.getFullName(), -1));
      }
    }

    return new TeacherStatsResponse(
        group.getFullName(),
        allStudents.size(),
        certifiedCount,
        failedCount,
        distribution,
        failedStudents);
  }

  private void requireAccess(User teacher, Group group, Subject subject) {
    boolean teachesGroup = teacher.getTeacherGroups().contains(group);
    boolean teachesSubject = subject.getTeachers().contains(teacher);
    boolean subjectForGroup = subject.getGroups().contains(group);

    if (!teachesGroup || !teachesSubject || !subjectForGroup) {
      throw new ForbiddenException("У вас нет доступа к этой группе или предмету");
    }
  }

  private List<Grade> findGroupGrades(Long subjectId, User teacher, List<User> students) {
    Set<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toSet());

    return gradeRepository.findBySubjectIdAndTeacherId(subjectId, teacher.getId()).stream()
        .filter(grade -> studentIds.contains(grade.getStudent().getId()))
        .toList();
  }

  private Optional<Grade> findStudentGrade(List<Grade> grades, User student) {
    return grades.stream()
        .filter(grade -> grade.getStudent().getId().equals(student.getId()))
        .findFirst();
  }

  private Map<Integer, Integer> emptyDistribution() {
    Map<Integer, Integer> distribution = new HashMap<>();
    distribution.put(5, 0);
    distribution.put(4, 0);
    distribution.put(3, 0);
    distribution.put(2, 0);
    distribution.put(0, 0); // 0 = неявка
    return distribution;
  }
}
