package ru.ulstu.grade_analysis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ulstu.grade_analysis.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  List<User> findByRoleName(String roleName);

  @Query(
      "SELECT DISTINCT u FROM User u WHERE u.role.name = 'STUDENT' "
          + "AND (:search IS NULL OR "
          + "LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', COALESCE(u.middleName, ''))) "
          + "LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) "
          + "AND (:course IS NULL OR u.course = :course) "
          + "AND (:level IS NULL OR u.educationLevel = :level)")
  Page<User> findFilteredStudents(
      @Param("search") String search,
      @Param("course") String course,
      @Param("level") String level,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT u FROM User u LEFT JOIN u.teacherGroups g WHERE u.role.name = 'TEACHER' "
          + "AND (:search IS NULL OR "
          + "LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', COALESCE(u.middleName, ''))) "
          + "LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) "
          + "AND (:groupId IS NULL OR g.id = :groupId)")
  Page<User> findFilteredTeachers(
      @Param("search") String search, @Param("groupId") Long groupId, Pageable pageable);

  @Query(
      "SELECT DISTINCT u FROM User u JOIN u.studentGroups g WHERE g.id = :groupId "
          + "AND (:search IS NULL OR "
          + "LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', COALESCE(u.middleName, ''))) "
          + "LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
  Page<User> findStudentsInGroup(
      @Param("groupId") Long groupId, @Param("search") String search, Pageable pageable);
}
