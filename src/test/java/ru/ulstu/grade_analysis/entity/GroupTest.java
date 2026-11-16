package ru.ulstu.grade_analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GroupTest {

  @Test
  void getFullName_combinesFields() {
    Group group = new Group();
    group.setPrefix("ПИбд");
    group.setCourse("1");
    group.setSubgroup("2");
    assertThat(group.getFullName()).isEqualTo("ПИбд-12");
  }
}
