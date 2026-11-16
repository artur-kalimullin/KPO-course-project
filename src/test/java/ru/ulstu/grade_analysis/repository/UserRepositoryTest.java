package ru.ulstu.grade_analysis.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Group.EducationLevel;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private EntityManager em;

  private final Pageable all = PageRequest.of(0, 100, Sort.by("lastName", "firstName"));

  private User s1;
  private User s2;
  private User s3;
  private User t1;
  private User t2;
  private Group group1;
  private Group group2;

  @BeforeEach
  void setUp() {
    Role studentRole = roleRepository.save(new Role(null, "STUDENT"));
    Role teacherRole = roleRepository.save(new Role(null, "TEACHER"));

    group1 =
        groupRepository.save(
            new Group(
                null, "ПИбд", "1", "1", EducationLevel.BACHELOR, new HashSet<>(), new HashSet<>()));
    group2 =
        groupRepository.save(
            new Group(
                null, "ПИбд", "2", "1", EducationLevel.BACHELOR, new HashSet<>(), new HashSet<>()));

    s1 =
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
    s2 =
        userRepository.save(
            new User(
                null,
                "s2",
                "hash",
                "Петров",
                "Пётр",
                "Петрович",
                studentRole,
                "2",
                "222222",
                "Очная",
                "Магистратура",
                new HashSet<>(),
                new HashSet<>()));
    s3 =
        userRepository.save(
            new User(
                null,
                "s3",
                "hash",
                "Сидоров",
                "Сидор",
                null,
                studentRole,
                "1",
                "333333",
                "Очная",
                "Бакалавриат",
                new HashSet<>(),
                new HashSet<>()));

    t1 =
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
    t2 =
        userRepository.save(
            new User(
                null,
                "t2",
                "hash",
                "Кузнецов",
                "Дмитрий",
                null,
                teacherRole,
                null,
                null,
                null,
                null,
                new HashSet<>(),
                new HashSet<>()));

    s1.getStudentGroups().add(group1);
    s2.getStudentGroups().add(group2);
    group1.getStudents().add(s1);
    group2.getStudents().add(s2);

    t1.getTeacherGroups().add(group1);
    t2.getTeacherGroups().add(group2);
    group1.getTeachers().add(t1);
    group2.getTeachers().add(t2);

    groupRepository.save(group1);
    groupRepository.save(group2);
    em.flush();
  }

  @Test
  void findByUsername_found() {
    assertThat(userRepository.findByUsername("s1")).contains(s1);
  }

  @Test
  void findByUsername_notFound() {
    assertThat(userRepository.findByUsername("unknown")).isEmpty();
  }

  @Test
  void findByRoleName_returnsOnlyStudents() {
    assertThat(userRepository.findByRoleName("STUDENT")).containsExactlyInAnyOrder(s1, s2, s3);
  }

  @Test
  void findByRoleName_returnsOnlyTeachers() {
    assertThat(userRepository.findByRoleName("TEACHER")).containsExactlyInAnyOrder(t1, t2);
  }

  @Test
  void findByRoleName_emptyForUnknownRole() {
    assertThat(userRepository.findByRoleName("ADMIN")).isEmpty();
  }

  @Test
  void findFilteredStudents_allNull_returnsAll() {
    Page<User> page = userRepository.findFilteredStudents(null, null, null, all);
    assertThat(page.getContent()).hasSize(3);
  }

  @Test
  void findFilteredStudents_bySearchLastName() {
    Page<User> page = userRepository.findFilteredStudents("Иванов", null, null, all);
    assertThat(page.getContent()).containsExactly(s1);
  }

  @Test
  void findFilteredStudents_bySearchFirstName() {
    Page<User> page = userRepository.findFilteredStudents("Пётр", null, null, all);
    assertThat(page.getContent()).containsExactly(s2);
  }

  @Test
  void findFilteredStudents_searchMatchesWhenMiddleNameNull() {
    Page<User> page = userRepository.findFilteredStudents("Сидоров", null, null, all);
    assertThat(page.getContent()).containsExactly(s3);
  }

  @Test
  void findFilteredStudents_byCourse() {
    Page<User> page = userRepository.findFilteredStudents(null, "1", null, all);
    assertThat(page.getContent()).containsExactlyInAnyOrder(s1, s3);
  }

  @Test
  void findFilteredStudents_byLevel() {
    Page<User> page = userRepository.findFilteredStudents(null, null, "Магистратура", all);
    assertThat(page.getContent()).containsExactly(s2);
  }

  @Test
  void findFilteredStudents_byCourseAndLevel() {
    Page<User> page = userRepository.findFilteredStudents(null, "1", "Бакалавриат", all);
    assertThat(page.getContent()).containsExactlyInAnyOrder(s1, s3);
  }

  @Test
  void findFilteredStudents_noMatch() {
    Page<User> page = userRepository.findFilteredStudents("Несуществующий", null, null, all);
    assertThat(page.getContent()).isEmpty();
  }

  @Test
  void findFilteredTeachers_allNull() {
    Page<User> page = userRepository.findFilteredTeachers(null, null, all);
    assertThat(page.getContent()).containsExactlyInAnyOrder(t1, t2);
  }

  @Test
  void findFilteredTeachers_bySearch() {
    Page<User> page = userRepository.findFilteredTeachers("Смирнов", null, all);
    assertThat(page.getContent()).containsExactly(t1);
  }

  @Test
  void findFilteredTeachers_byGroupId() {
    Page<User> page = userRepository.findFilteredTeachers(null, group1.getId(), all);
    assertThat(page.getContent()).containsExactly(t1);
  }

  @Test
  void findFilteredTeachers_bySearchAndGroup() {
    Page<User> page = userRepository.findFilteredTeachers("Смирнов", group1.getId(), all);
    assertThat(page.getContent()).containsExactly(t1);
  }

  @Test
  void findFilteredTeachers_bySearchAndWrongGroup_empty() {
    Page<User> page = userRepository.findFilteredTeachers("Смирнов", group2.getId(), all);
    assertThat(page.getContent()).isEmpty();
  }

  @Test
  void findStudentsInGroup_noSearch() {
    Page<User> page = userRepository.findStudentsInGroup(group1.getId(), null, all);
    assertThat(page.getContent()).containsExactly(s1);
  }

  @Test
  void findStudentsInGroup_bySearch() {
    Page<User> page = userRepository.findStudentsInGroup(group1.getId(), "Иванов", all);
    assertThat(page.getContent()).containsExactly(s1);
  }

  @Test
  void findStudentsInGroup_searchNoMatch_empty() {
    Page<User> page = userRepository.findStudentsInGroup(group1.getId(), "Петров", all);
    assertThat(page.getContent()).isEmpty();
  }

  @Test
  void findStudentsInGroup_unknownGroup_empty() {
    Page<User> page = userRepository.findStudentsInGroup(999L, null, all);
    assertThat(page.getContent()).isEmpty();
  }
}
