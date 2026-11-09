package ru.ulstu.grade_analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 40)
  private String username;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "last_name", nullable = false, length = 60)
  private String lastName;

  @Column(name = "first_name", nullable = false, length = 60)
  private String firstName;

  @Column(name = "middle_name", length = 60)
  private String middleName;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(name = "course", length = 1)
  private String course;

  @Column(name = "record_book_number", unique = true, length = 6)
  private String recordBookNumber;

  @Column(name = "education_form", length = 20)
  private String educationForm;

  @Column(name = "education_level", length = 20)
  private String educationLevel;

  @ManyToMany(mappedBy = "students")
  private Set<Group> studentGroups = new HashSet<>();

  @ManyToMany(mappedBy = "teachers")
  private Set<Group> teacherGroups = new HashSet<>();

  public String getFullName() {
    StringBuilder sb = new StringBuilder();
    if (lastName != null) {
      sb.append(lastName).append(" ");
    }
    if (firstName != null) {
      sb.append(firstName).append(" ");
    }
    if (middleName != null && !middleName.isEmpty()) {
      sb.append(middleName);
    }
    return sb.toString().trim();
  }
}
