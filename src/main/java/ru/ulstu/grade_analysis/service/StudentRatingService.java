package ru.ulstu.grade_analysis.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.dto.RatingResponse;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@Service
public class StudentRatingService {

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;

  public StudentRatingService(UserRepository userRepository, GradeRepository gradeRepository) {
    this.userRepository = userRepository;
    this.gradeRepository = gradeRepository;
  }

  @Transactional(readOnly = true)
  public RatingResponse getGroupRating(String username) {
    User currentStudent =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Студент не найден"));

    Group group =
        currentStudent.getStudentGroups().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Студент не привязан к группе"));

    List<RatingResponse.RatingEntry> ranking = new ArrayList<>();
    for (User student : group.getStudents()) {
      ranking.add(buildEntry(student));
    }

    ranking.sort((a, b) -> Double.compare(b.average(), a.average()));

    int myPlace = findPlace(ranking, currentStudent.getId());
    List<RatingResponse.RatingEntry> top3 = ranking.stream().limit(3).toList();

    long studentsWithDebts = ranking.stream().filter(RatingResponse.RatingEntry::hasDebts).count();
    long studentsWithoutDebts = ranking.size() - studentsWithDebts;

    double myAverage =
        ranking.stream()
            .filter(entry -> entry.id().equals(currentStudent.getId()))
            .findFirst()
            .map(RatingResponse.RatingEntry::average)
            .orElse(0.0);

    return new RatingResponse(
        group.getFullName(),
        top3,
        myPlace,
        myAverage,
        ranking.size(),
        studentsWithDebts,
        studentsWithoutDebts);
  }

  private RatingResponse.RatingEntry buildEntry(User student) {
    List<Grade> grades = gradeRepository.findByStudentId(student.getId());

    double sum = 0;
    int count = 0;
    boolean hasDebts = false;

    for (Grade grade : grades) {
      if (grade.getValue() >= 2 && grade.getValue() <= 5) {
        sum += grade.getValue();
        count++;
      }
      if (grade.getValue() == 2 || grade.getValue() == 0) {
        hasDebts = true;
      }
    }

    double average = count > 0 ? Math.round((sum / count) * 100.0) / 100.0 : 0.0;

    return new RatingResponse.RatingEntry(
        student.getId(), student.getFullName(), average, hasDebts);
  }

  private int findPlace(List<RatingResponse.RatingEntry> ranking, Long studentId) {
    for (int i = 0; i < ranking.size(); i++) {
      if (ranking.get(i).id().equals(studentId)) {
        return i + 1;
      }
    }
    return 1;
  }
}
