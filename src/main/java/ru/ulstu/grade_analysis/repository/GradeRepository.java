package ru.ulstu.grade_analysis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.grade_analysis.entity.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {
  List<Grade> findByStudentId(Long studentId);

  List<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

  Optional<Grade> findByStudentIdAndSubjectIdAndTeacherId(
      Long studentId, Long subjectId, Long teacherId);

  List<Grade> findBySubjectIdAndTeacherId(Long subjectId, Long teacherId);
}
