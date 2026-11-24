package ru.ulstu.grade_analysis.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.ulstu.grade_analysis.dto.GroupResponse.StudentShortDto;
import ru.ulstu.grade_analysis.dto.SubjectResponse.GroupShortDto;
import ru.ulstu.grade_analysis.dto.SubjectResponse.TeacherShortDto;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;

class DtoTest {

  // ---------- Login ----------
  @Test
  void loginRequest_accessors() {
    LoginRequest r = new LoginRequest("user", "pass");
    assertThat(r.username()).isEqualTo("user");
    assertThat(r.password()).isEqualTo("pass");
  }

  @Test
  void loginResponse_accessors() {
    LoginResponse r = new LoginResponse("token", "ROLE_ADMIN", "admin");
    assertThat(r.token()).isEqualTo("token");
    assertThat(r.role()).isEqualTo("ROLE_ADMIN");
    assertThat(r.username()).isEqualTo("admin");
  }

  // ---------- Group ----------
  @Test
  void groupRequest_accessors() {
    GroupRequest r = new GroupRequest("ПИбд", "1", "1", EducationLevel.BACHELOR);
    assertThat(r.prefix()).isEqualTo("ПИбд");
    assertThat(r.course()).isEqualTo("1");
    assertThat(r.subgroup()).isEqualTo("1");
    assertThat(r.educationLevel()).isEqualTo(EducationLevel.BACHELOR);
  }

  @Test
  void groupResponse_accessors() {
    StudentShortDto s = new StudentShortDto(10L, "Иванов Иван");
    GroupResponse r =
        new GroupResponse(1L, "ПИбд", "1", "1", EducationLevel.BACHELOR, "ПИбд-11", 1, List.of(s));
    assertThat(r.id()).isEqualTo(1L);
    assertThat(r.prefix()).isEqualTo("ПИбд");
    assertThat(r.course()).isEqualTo("1");
    assertThat(r.subgroup()).isEqualTo("1");
    assertThat(r.educationLevel()).isEqualTo(EducationLevel.BACHELOR);
    assertThat(r.fullName()).isEqualTo("ПИбд-11");
    assertThat(r.studentsCount()).isEqualTo(1);
    assertThat(r.students()).containsExactly(s);
  }

  @Test
  void studentShortDto_accessors() {
    StudentShortDto s = new StudentShortDto(1L, "X");
    assertThat(s.id()).isEqualTo(1L);
    assertThat(s.fullName()).isEqualTo("X");
  }

  // ---------- Student ----------
  @Test
  void studentCreateRequest_accessors() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "student",
            "pass",
            "Иван",
            "Иванов",
            "Иванович",
            "1",
            1L,
            "123456",
            "Очная",
            "Бакалавриат");
    assertThat(r.username()).isEqualTo("student");
    assertThat(r.password()).isEqualTo("pass");
    assertThat(r.firstName()).isEqualTo("Иван");
    assertThat(r.lastName()).isEqualTo("Иванов");
    assertThat(r.middleName()).isEqualTo("Иванович");
    assertThat(r.course()).isEqualTo("1");
    assertThat(r.groupId()).isEqualTo(1L);
    assertThat(r.recordBookNumber()).isEqualTo("123456");
    assertThat(r.educationForm()).isEqualTo("Очная");
    assertThat(r.educationLevel()).isEqualTo("Бакалавриат");
  }

  @Test
  void studentResponse_accessors() {
    StudentResponse r = new StudentResponse(1L, "Ivanov Ivan", "s1", "1", "123456");
    assertThat(r.id()).isEqualTo(1L);
    assertThat(r.fullName()).isEqualTo("Ivanov Ivan");
    assertThat(r.username()).isEqualTo("s1");
    assertThat(r.course()).isEqualTo("1");
    assertThat(r.recordBookNumber()).isEqualTo("123456");
  }

  // ---------- Subject ----------
  @Test
  void subjectRequest_accessors() {
    SubjectRequest r = new SubjectRequest("Math", List.of(1L, 2L), List.of(10L));
    assertThat(r.name()).isEqualTo("Math");
    assertThat(r.teacherIds()).containsExactly(1L, 2L);
    assertThat(r.groupIds()).containsExactly(10L);
  }

  @Test
  void subjectResponse_accessors() {
    TeacherShortDto t = new TeacherShortDto(1L, "Teacher T");
    GroupShortDto g = new GroupShortDto(2L, "Group G");
    SubjectResponse r = new SubjectResponse(10L, "Math", 1, List.of(t), List.of(g));
    assertThat(r.id()).isEqualTo(10L);
    assertThat(r.name()).isEqualTo("Math");
    assertThat(r.teachersCount()).isEqualTo(1);
    assertThat(r.teachers()).containsExactly(t);
    assertThat(r.groups()).containsExactly(g);
  }

  @Test
  void teacherShortDto_accessors() {
    TeacherShortDto t = new TeacherShortDto(1L, "T");
    assertThat(t.id()).isEqualTo(1L);
    assertThat(t.fullName()).isEqualTo("T");
  }

  @Test
  void groupShortDto_accessors() {
    GroupShortDto g = new GroupShortDto(2L, "G");
    assertThat(g.id()).isEqualTo(2L);
    assertThat(g.fullName()).isEqualTo("G");
  }

  // ---------- Teacher ----------
  @Test
  void teacherCreateRequest_accessors() {
    TeacherCreateRequest r =
        new TeacherCreateRequest("teacher", "pass", "Пётр", "Петров", "Петрович", List.of(1L));
    assertThat(r.username()).isEqualTo("teacher");
    assertThat(r.password()).isEqualTo("pass");
    assertThat(r.firstName()).isEqualTo("Пётр");
    assertThat(r.lastName()).isEqualTo("Петров");
    assertThat(r.middleName()).isEqualTo("Петрович");
    assertThat(r.groupIds()).containsExactly(1L);
  }

  // ---------- Grade ----------
  @Test
  void gradeRequest_accessors() {
    GradeRequest r = new GradeRequest(10L, 1L, 5);
    assertThat(r.studentId()).isEqualTo(10L);
    assertThat(r.subjectId()).isEqualTo(1L);
    assertThat(r.value()).isEqualTo(5);
  }

  @Test
  void gradeResponse_accessors() {
    GradeResponse r = new GradeResponse(1L, 10L, "Ivanov Ivan", 20L, "Math", 30L, "P-11", 5);
    assertThat(r.id()).isEqualTo(1L);
    assertThat(r.studentId()).isEqualTo(10L);
    assertThat(r.studentFullName()).isEqualTo("Ivanov Ivan");
    assertThat(r.subjectId()).isEqualTo(20L);
    assertThat(r.subjectName()).isEqualTo("Math");
    assertThat(r.groupId()).isEqualTo(30L);
    assertThat(r.groupName()).isEqualTo("P-11");
    assertThat(r.value()).isEqualTo(5);
  }
}
