package ru.ulstu.grade_analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;

public interface GroupRepository extends JpaRepository<Group, Long> {
  @Query(
      "SELECT g FROM Group g WHERE "
          + "(:course IS NULL OR g.course = :course) AND "
          + "(:level IS NULL OR g.educationLevel = :level) AND "
          + "(:search IS NULL OR LOWER(CONCAT(g.prefix, '-', g.course, g.subgroup)) "
          + "LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%'))")
  Page<Group> findFiltered(
      @Param("course") String course,
      @Param("level") EducationLevel level,
      @Param("search") String search,
      Pageable pageable);
}
