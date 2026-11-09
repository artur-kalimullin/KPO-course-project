package ru.ulstu.grade_analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.grade_analysis.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);
}
