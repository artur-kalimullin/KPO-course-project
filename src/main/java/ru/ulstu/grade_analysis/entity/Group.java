package ru.ulstu.grade_analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Group {

  public enum EducationLevel {
    BACHELOR,
    MASTER
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String prefix;

  @Column(nullable = false, length = 1)
  private String course;

  @Column(nullable = false, length = 1)
  private String subgroup;

  @Enumerated(EnumType.STRING)
  @Column(name = "education_level", nullable = false)
  private EducationLevel educationLevel;

  @ManyToMany
  @JoinTable(
      name = "student_groups",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "student_id"))
  private Set<User> students = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "teacher_groups",
      joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "teacher_id"))
  private Set<User> teachers = new HashSet<>();

  public String getFullName() {
    return prefix + "-" + course + subgroup;
  }
}
