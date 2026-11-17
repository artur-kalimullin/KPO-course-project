package ru.ulstu.grade_analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GradeTest {

  @Test
  void allArgsConstructor_and_getters() {
    User student = new User();
    Subject subject = new Subject();
    User teacher = new User();
    Grade g = new Grade(1L, student, subject, teacher, 5);
    assertThat(g.getId()).isEqualTo(1L);
    assertThat(g.getStudent()).isSameAs(student);
    assertThat(g.getSubject()).isSameAs(subject);
    assertThat(g.getTeacher()).isSameAs(teacher);
    assertThat(g.getValue()).isEqualTo(5);
  }

  @Test
  void setters_forAllFields() {
    Grade g = new Grade();
    g.setId(2L);
    g.setStudent(new User());
    g.setSubject(new Subject());
    g.setTeacher(new User());
    g.setValue(4);
    assertThat(g.getId()).isEqualTo(2L);
    assertThat(g.getValue()).isEqualTo(4);
  }

  @Test
  void noArgsConstructor_createsEmpty() {
    Grade g = new Grade();
    assertThat(g.getId()).isNull();
    assertThat(g.getValue()).isNull();
  }
}
