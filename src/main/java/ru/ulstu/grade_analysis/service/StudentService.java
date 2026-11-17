package ru.ulstu.grade_analysis.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.StudentGradesResponse;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class StudentService {

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;

  public StudentService(UserRepository userRepository, GradeRepository gradeRepository) {
    this.userRepository = userRepository;
    this.gradeRepository = gradeRepository;
  }

  @Transactional(readOnly = true)
  public StudentGradesResponse getMyGrades(String username) {
    User student = findStudent(username);
    List<Grade> grades = gradeRepository.findByStudentId(student.getId());

    String groupName =
        student.getStudentGroups().stream()
            .map(Group::getFullName)
            .findFirst()
            .orElse("Без группы");

    List<StudentGradesResponse.GradeItem> gradeItems =
        grades.stream()
            .map(
                grade ->
                    new StudentGradesResponse.GradeItem(
                        grade.getSubject().getName(), grade.getValue()))
            .toList();

    double sum = 0;
    int count = 0;
    for (Grade grade : grades) {
      if (grade.getValue() >= 2 && grade.getValue() <= 5) {
        sum += grade.getValue();
        count++;
      }
    }
    double average = count > 0 ? Math.round((sum / count) * 100.0) / 100.0 : 0.0;

    List<String> debts =
        grades.stream()
            .filter(grade -> grade.getValue() == 2 || grade.getValue() == 0)
            .map(grade -> grade.getSubject().getName())
            .toList();

    return new StudentGradesResponse(student.getFullName(), groupName, gradeItems, average, debts);
  }

  private User findStudent(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException("Студент не найден"));
  }
}
