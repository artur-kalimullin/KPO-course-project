package ru.ulstu.grade_analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  void allArgsConstructor_and_getters() {
    Role r = new Role(1L, "ADMIN");
    assertThat(r.getId()).isEqualTo(1L);
    assertThat(r.getName()).isEqualTo("ADMIN");
  }

  @Test
  void setters_and_getters() {
    Role r = new Role();
    r.setId(2L);
    r.setName("TEACHER");
    assertThat(r.getId()).isEqualTo(2L);
    assertThat(r.getName()).isEqualTo("TEACHER");
  }

  @Test
  void noArgsConstructor_createsEmpty() {
    Role r = new Role();
    assertThat(r.getId()).isNull();
    assertThat(r.getName()).isNull();
  }
}
