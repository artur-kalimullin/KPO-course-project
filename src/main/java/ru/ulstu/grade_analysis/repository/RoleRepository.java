package ru.ulstu.grade_analysis.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.grade_analysis.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);
}
