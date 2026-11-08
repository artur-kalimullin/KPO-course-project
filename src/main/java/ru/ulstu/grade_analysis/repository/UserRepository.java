package ru.ulstu.grade_analysis.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.grade_analysis.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  List<User> findAllByRoleName(String roleName);

  List<User> findByRoleName(String roleName);
}
