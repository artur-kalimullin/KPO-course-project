package ru.ulstu.grade_analysis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.ulstu.grade_analysis.dto.GradeDto.GradeRequest;
import ru.ulstu.grade_analysis.dto.GroupRequest;
import ru.ulstu.grade_analysis.dto.LoginRequest;
import ru.ulstu.grade_analysis.dto.StudentCreateRequest;
import ru.ulstu.grade_analysis.dto.SubjectRequest;
import ru.ulstu.grade_analysis.dto.TeacherCreateRequest;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;

class ValidationExtendedTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  // ---------- LoginRequest ----------
  @Test
  void loginRequest_valid() {
    assertThat(validator.validate(new LoginRequest("u", "p"))).isEmpty();
  }

  @Test
  void loginRequest_blankUsername() {
    assertThat(validator.validate(new LoginRequest("", "p"))).isNotEmpty();
  }

  @Test
  void loginRequest_blankPassword() {
    assertThat(validator.validate(new LoginRequest("u", ""))).isNotEmpty();
  }

  // ---------- GroupRequest ----------
  @Test
  void groupRequest_valid() {
    assertThat(validator.validate(new GroupRequest("ПИбд", "1", "1", EducationLevel.BACHELOR)))
        .isEmpty();
  }

  @Test
  void groupRequest_blankPrefix() {
    assertThat(validator.validate(new GroupRequest("", "1", "1", EducationLevel.BACHELOR)))
        .isNotEmpty();
  }

  @Test
  void groupRequest_prefixTooLong() {
    String prefix = "a".repeat(21);
    assertThat(validator.validate(new GroupRequest(prefix, "1", "1", EducationLevel.BACHELOR)))
        .isNotEmpty();
  }

  @Test
  void groupRequest_blankCourse() {
    assertThat(validator.validate(new GroupRequest("ПИбд", "", "1", EducationLevel.BACHELOR)))
        .isNotEmpty();
  }

  @Test
  void groupRequest_invalidSubgroupLetter() {
    assertThat(validator.validate(new GroupRequest("ПИбд", "1", "A", EducationLevel.BACHELOR)))
        .isNotEmpty();
  }

  @Test
  void groupRequest_invalidSubgroupZero() {
    assertThat(validator.validate(new GroupRequest("ПИбд", "1", "0", EducationLevel.BACHELOR)))
        .isNotEmpty();
  }

  @Test
  void groupRequest_nullEducationLevel() {
    assertThat(validator.validate(new GroupRequest("ПИбд", "1", "1", null))).isNotEmpty();
  }

  // ---------- StudentCreateRequest ----------
  private static StudentCreateRequest validStudent() {
    return new StudentCreateRequest(
        "student", "pass", "Иван", "Иванов", "Иванович", "1", 1L, "123456", "Очная", "Бакалавриат");
  }

  @Test
  void studentRequest_valid() {
    assertThat(validator.validate(validStudent())).isEmpty();
  }

  @Test
  void studentRequest_nullPasswordAllowed() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "student", null, "Иван", "Иванов", null, "1", 1L, "123456", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isEmpty();
  }

  @Test
  void studentRequest_shortUsername() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "a", "p", "Иван", "Иванов", null, "1", 1L, "123456", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isNotEmpty();
  }

  @Test
  void studentRequest_longUsername() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "a".repeat(41), "p", "Иван", "Иванов", null, "1", 1L, "123456", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isNotEmpty();
  }

  @Test
  void studentRequest_blankFirstName() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "student", "p", "", "Иванов", null, "1", 1L, "123456", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isNotEmpty();
  }

  @Test
  void studentRequest_blankLastName() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "student", "p", "Иван", "", null, "1", 1L, "123456", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isNotEmpty();
  }

  @Test
  void studentRequest_invalidRecordBookTooShort() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "student", "p", "Иван", "Иванов", null, "1", 1L, "123", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isNotEmpty();
  }

  @Test
  void studentRequest_invalidRecordBookWithLetters() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "student", "p", "Иван", "Иванов", null, "1", 1L, "abcdef", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isNotEmpty();
  }

  @Test
  void studentRequest_nullGroupId() {
    StudentCreateRequest r =
        new StudentCreateRequest(
            "student", "p", "Иван", "Иванов", null, "1", null, "123456", "Очная", "Бакалавриат");
    assertThat(validator.validate(r)).isNotEmpty();
  }

  // ---------- TeacherCreateRequest ----------
  @Test
  void teacherRequest_valid() {
    assertThat(
            validator.validate(
                new TeacherCreateRequest("teacher", "pass", "Пётр", "Петров", null, null)))
        .isEmpty();
  }

  @Test
  void teacherRequest_shortUsername() {
    assertThat(
            validator.validate(new TeacherCreateRequest("t", "pass", "Пётр", "Петров", null, null)))
        .isNotEmpty();
  }

  @Test
  void teacherRequest_blankFirstName() {
    assertThat(
            validator.validate(new TeacherCreateRequest("teacher", "p", "", "Петров", null, null)))
        .isNotEmpty();
  }

  @Test
  void teacherRequest_blankLastName() {
    assertThat(validator.validate(new TeacherCreateRequest("teacher", "p", "Пётр", "", null, null)))
        .isNotEmpty();
  }

  // ---------- SubjectRequest ----------
  @Test
  void subjectRequest_valid() {
    assertThat(validator.validate(new SubjectRequest("Math", List.of(1L), List.of(1L)))).isEmpty();
  }

  @Test
  void subjectRequest_nullListsAllowed() {
    assertThat(validator.validate(new SubjectRequest("Math", null, null))).isEmpty();
  }

  @Test
  void subjectRequest_blankName() {
    assertThat(validator.validate(new SubjectRequest("", null, null))).isNotEmpty();
  }

  @Test
  void subjectRequest_nameTooLong() {
    assertThat(validator.validate(new SubjectRequest("a".repeat(201), null, null))).isNotEmpty();
  }

  // ---------- GradeDto.GradeRequest ----------
  @Test
  void gradeRequest_valid() {
    assertThat(validator.validate(new GradeRequest(1L, 2L, 5))).isEmpty();
  }

  @Test
  void gradeRequest_nullStudentId() {
    assertThat(validator.validate(new GradeRequest(null, 2L, 5))).isNotEmpty();
  }

  @Test
  void gradeRequest_nullSubjectId() {
    assertThat(validator.validate(new GradeRequest(1L, null, 5))).isNotEmpty();
  }

  @Test
  void gradeRequest_valueTooLow() {
    assertThat(validator.validate(new GradeRequest(1L, 2L, -1))).isNotEmpty();
  }

  @Test
  void gradeRequest_valueTooHigh() {
    assertThat(validator.validate(new GradeRequest(1L, 2L, 6))).isNotEmpty();
  }

  @Test
  void gradeRequest_nullValue() {
    assertThat(validator.validate(new GradeRequest(1L, 2L, null))).isNotEmpty();
  }
}
