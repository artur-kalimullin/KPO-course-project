package ru.ulstu.grade_analysis.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GradeRepositoryTest {

  @Autowired private GradeRepository gradeRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private SubjectRepository subjectRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private EntityManager em;

  private User student1;
  private User student2;
  private User teacher1;
  private Subject subject1;
  private Grade g1;

  @BeforeEach
  void setUp() {
    Role studentRole = roleRepository.save(new Role(null, "STUDENT"));
    Role teacherRole = roleRepository.save(new Role(null, "TEACHER"));

    student1 =
        userRepository.save(
            new User(
                null,
                "s1",
                "hash",
                "Иванов",
                "Иван",
                null,
                studentRole,
                "1",
                "111111",
                "Очная",
                "Бакалавриат",
                new HashSet<>(),
                new HashSet<>()));
    student2 =
        userRepository.save(
            new User(
                null,
                "s2",
                "hash",
                "Петров",
                "Пётр",
                null,
                studentRole,
                "2",
                "222222",
                "Очная",
                "Бакалавриат",
                new HashSet<>(),
                new HashSet<>()));
    teacher1 =
        userRepository.save(
            new User(
                null,
                "t1",
                "hash",
                "Смирнов",
                "Алексей",
                null,
                teacherRole,
                null,
                null,
                null,
                null,
                new HashSet<>(),
                new HashSet<>()));

    subject1 = subjectRepository.save(new Subject(null, "Math", new HashSet<>(), new HashSet<>()));

    g1 = gradeRepository.save(new Grade(null, student1, subject1, teacher1, 5));
    gradeRepository.save(new Grade(null, student2, subject1, teacher1, 3));
    em.flush();
  }

  @Test
  void findByStudentId_returnsOnlyStudentGrades() {
    assertThat(gradeRepository.findByStudentId(student1.getId())).containsExactly(g1);
  }

  @Test
  void findByStudentId_emptyForUnknownStudent() {
    assertThat(gradeRepository.findByStudentId(999L)).isEmpty();
  }

  @Test
  void findByStudentIdAndSubjectId_matches() {
    assertThat(gradeRepository.findByStudentIdAndSubjectId(student1.getId(), subject1.getId()))
        .hasSize(1);
  }

  @Test
  void findByStudentIdAndSubjectId_emptyWhenNoMatch() {
    assertThat(gradeRepository.findByStudentIdAndSubjectId(student1.getId(), 999L)).isEmpty();
  }

  @Test
  void findByStudentIdAndSubjectIdAndTeacherId_found() {
    assertThat(
            gradeRepository.findByStudentIdAndSubjectIdAndTeacherId(
                student1.getId(), subject1.getId(), teacher1.getId()))
        .contains(g1);
  }

  @Test
  void findByStudentIdAndSubjectIdAndTeacherId_emptyWhenWrongTeacher() {
    assertThat(
            gradeRepository.findByStudentIdAndSubjectIdAndTeacherId(
                student1.getId(), subject1.getId(), 999L))
        .isEmpty();
  }

  @Test
  void findBySubjectIdAndTeacherId_returnsAllForSubjectAndTeacher() {
    assertThat(gradeRepository.findBySubjectIdAndTeacherId(subject1.getId(), teacher1.getId()))
        .hasSize(2);
  }

  @Test
  void findBySubjectIdAndTeacherId_emptyWhenWrongTeacher() {
    assertThat(gradeRepository.findBySubjectIdAndTeacherId(subject1.getId(), 999L)).isEmpty();
  }
}
