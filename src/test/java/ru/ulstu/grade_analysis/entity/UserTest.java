package ru.ulstu.grade_analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class UserTest {

  private static User fullUser() {
    Role role = new Role(1L, "ADMIN");
    User u =
        new User(
            10L,
            "admin",
            "hash",
            "Иванов",
            "Иван",
            "Иванович",
            role,
            "1",
            "123456",
            "Очная",
            "Бакалавриат",
            new HashSet<>(),
            new HashSet<>());
    return u;
  }

  @Test
  void getFullName_allFieldsPresent() {
    User u = new User();
    u.setLastName("Иванов");
    u.setFirstName("Иван");
    u.setMiddleName("Иванович");
    assertThat(u.getFullName()).isEqualTo("Иванов Иван Иванович");
  }

  @Test
  void getFullName_middleNameNull() {
    User u = new User();
    u.setLastName("Иванов");
    u.setFirstName("Иван");
    u.setMiddleName(null);
    assertThat(u.getFullName()).isEqualTo("Иванов Иван");
  }

  @Test
  void getFullName_middleNameEmpty() {
    User u = new User();
    u.setLastName("Иванов");
    u.setFirstName("Иван");
    u.setMiddleName("");
    assertThat(u.getFullName()).isEqualTo("Иванов Иван");
  }

  @Test
  void getFullName_lastNameNull() {
    User u = new User();
    u.setLastName(null);
    u.setFirstName("Иван");
    u.setMiddleName("Иванович");
    assertThat(u.getFullName()).isEqualTo("Иван Иванович");
  }

  @Test
  void getFullName_firstNameNull() {
    User u = new User();
    u.setLastName("Иванов");
    u.setFirstName(null);
    u.setMiddleName("Иванович");
    assertThat(u.getFullName()).isEqualTo("Иванов Иванович");
  }

  @Test
  void getFullName_allNamesNull() {
    User u = new User();
    u.setLastName(null);
    u.setFirstName(null);
    u.setMiddleName(null);
    assertThat(u.getFullName()).isEmpty();
  }

  @Test
  void getFullName_lastAndFirstNull_middleEmpty() {
    User u = new User();
    u.setLastName(null);
    u.setFirstName(null);
    u.setMiddleName("");
    assertThat(u.getFullName()).isEmpty();
  }

  @Test
  void settersAndGetters_forAllFields() {
    User u = fullUser();
    assertThat(u.getId()).isEqualTo(10L);
    assertThat(u.getUsername()).isEqualTo("admin");
    assertThat(u.getPasswordHash()).isEqualTo("hash");
    assertThat(u.getLastName()).isEqualTo("Иванов");
    assertThat(u.getFirstName()).isEqualTo("Иван");
    assertThat(u.getMiddleName()).isEqualTo("Иванович");
    assertThat(u.getRole().getName()).isEqualTo("ADMIN");
    assertThat(u.getCourse()).isEqualTo("1");
    assertThat(u.getRecordBookNumber()).isEqualTo("123456");
    assertThat(u.getEducationForm()).isEqualTo("Очная");
    assertThat(u.getEducationLevel()).isEqualTo("Бакалавриат");
    assertThat(u.getStudentGroups()).isEmpty();
    assertThat(u.getTeacherGroups()).isEmpty();
  }

  @Test
  void noArgsConstructor_initializesCollections() {
    User u = new User();
    assertThat(u.getStudentGroups()).isNotNull().isEmpty();
    assertThat(u.getTeacherGroups()).isNotNull().isEmpty();
  }

  @Test
  void collectionsAreMutable() {
    User u = new User();
    Group g = new Group();
    g.setId(1L);
    u.getStudentGroups().add(g);
    u.getTeacherGroups().add(g);
    assertThat(u.getStudentGroups()).hasSize(1);
    assertThat(u.getTeacherGroups()).hasSize(1);
  }
}
