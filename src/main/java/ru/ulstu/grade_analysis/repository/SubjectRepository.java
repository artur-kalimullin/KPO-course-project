package ru.ulstu.grade_analysis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ulstu.grade_analysis.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);

  @Query(
      "SELECT DISTINCT s FROM Subject s "
          + "LEFT JOIN s.teachers t "
          + "LEFT JOIN s.groups g WHERE "
          + "(:search IS NULL OR LOWER(s.name) LIKE CONCAT('%', LOWER(CAST(:search AS string)), '%')) AND "
          + "(:teacherId IS NULL OR t.id = :teacherId) AND "
          + "(:groupId IS NULL OR g.id = :groupId)")
  Page<Subject> findFiltered(
      @Param("search") String search,
      @Param("teacherId") Long teacherId,
      @Param("groupId") Long groupId,
      Pageable pageable);
}
