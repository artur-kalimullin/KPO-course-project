package ru.ulstu.grade_analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.grade_analysis.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {}
