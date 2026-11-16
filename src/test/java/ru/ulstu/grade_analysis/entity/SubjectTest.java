package ru.ulstu.grade_analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SubjectTest {

  @Test
  void allArgsConstructor_and_getters() {
    User t = new User();
    Group g = new Group();
    Subject s = new Subject(1L, "Math", new HashSet<>(Set.of(t)), new HashSet<>(Set.of(g)));
    assertThat(s.getId()).isEqualTo(1L);
    assertThat(s.getName()).isEqualTo("Math");
    assertThat(s.getTeachers()).containsExactly(t);
    assertThat(s.getGroups()).containsExactly(g);
  }

  @Test
  void setters_forAllFields() {
    Subject s = new Subject();
    s.setId(5L);
    s.setName("Physics");
    s.getTeachers().add(new User());
    s.getGroups().add(new Group());
    assertThat(s.getId()).isEqualTo(5L);
    assertThat(s.getName()).isEqualTo("Physics");
    assertThat(s.getTeachers()).hasSize(1);
    assertThat(s.getGroups()).hasSize(1);
  }

  @Test
  void noArgsConstructor_initializesCollections() {
    Subject s = new Subject();
    assertThat(s.getTeachers()).isNotNull().isEmpty();
    assertThat(s.getGroups()).isNotNull().isEmpty();
  }
}
