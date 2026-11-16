package ru.ulstu.grade_analysis.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubjectRepositoryTest {

  @Autowired private SubjectRepository subjectRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private EntityManager em;

  private final Pageable all = PageRequest.of(0, 100, Sort.by("name"));

  private Subject math;
  private Subject physics;
  private Subject chemistry;
  private User teacher1;
  private User teacher2;
  private Group group1;
  private Group group2;

  @BeforeEach
  void setUp() {
    Role teacherRole = roleRepository.save(new Role(null, "TEACHER"));
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
    teacher2 =
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

    group1 =
        groupRepository.save(
            new Group(
                null, "ПИбд", "1", "1", EducationLevel.BACHELOR, new HashSet<>(), new HashSet<>()));
    group2 =
        groupRepository.save(
            new Group(
                null, "ПИбд", "2", "1", EducationLevel.BACHELOR, new HashSet<>(), new HashSet<>()));

    math =
        new Subject(null, "Math", new HashSet<>(Set.of(teacher1)), new HashSet<>(Set.of(group1)));
    physics =
        new Subject(
            null, "Physics", new HashSet<>(Set.of(teacher2)), new HashSet<>(Set.of(group2)));
    chemistry = new Subject(null, "Chemistry", new HashSet<>(), new HashSet<>());

    subjectRepository.saveAll(List.of(math, physics, chemistry));
    em.flush();
  }

  @Test
  void existsByName_true() {
    assertThat(subjectRepository.existsByName("Math")).isTrue();
  }

  @Test
  void existsByName_false() {
    assertThat(subjectRepository.existsByName("Unknown")).isFalse();
  }

  @Test
  void existsByNameAndIdNot_false_whenSameSubject() {
    assertThat(subjectRepository.existsByNameAndIdNot("Math", math.getId())).isFalse();
  }

  @Test
  void existsByNameAndIdNot_true_whenOtherHasName() {
    assertThat(subjectRepository.existsByNameAndIdNot("Math", physics.getId())).isTrue();
  }

  @Test
  void existsByNameAndIdNot_false_whenNewName() {
    assertThat(subjectRepository.existsByNameAndIdNot("NewSubject", math.getId())).isFalse();
  }

  @Test
  void findFiltered_allNull_returnsAll() {
    Page<Subject> page = subjectRepository.findFiltered(null, null, null, all);
    assertThat(page.getContent()).hasSize(3);
  }

  @Test
  void findFiltered_bySearch() {
    Page<Subject> page = subjectRepository.findFiltered("Math", null, null, all);
    assertThat(page.getContent()).containsExactly(math);
  }

  @Test
  void findFiltered_bySearch_caseInsensitive() {
    Page<Subject> page = subjectRepository.findFiltered("math", null, null, all);
    assertThat(page.getContent()).containsExactly(math);
  }

  @Test
  void findFiltered_byTeacherId() {
    Page<Subject> page = subjectRepository.findFiltered(null, teacher1.getId(), null, all);
    assertThat(page.getContent()).containsExactly(math);
  }

  @Test
  void findFiltered_byGroupId() {
    Page<Subject> page = subjectRepository.findFiltered(null, null, group1.getId(), all);
    assertThat(page.getContent()).containsExactly(math);
  }

  @Test
  void findFiltered_byTeacherAndGroup() {
    Page<Subject> page =
        subjectRepository.findFiltered(null, teacher1.getId(), group1.getId(), all);
    assertThat(page.getContent()).containsExactly(math);
  }

  @Test
  void findFiltered_byTeacherAndWrongGroup_empty() {
    Page<Subject> page =
        subjectRepository.findFiltered(null, teacher1.getId(), group2.getId(), all);
    assertThat(page.getContent()).isEmpty();
  }

  @Test
  void findFiltered_byAllThree() {
    Page<Subject> page =
        subjectRepository.findFiltered("Math", teacher1.getId(), group1.getId(), all);
    assertThat(page.getContent()).containsExactly(math);
  }
}
