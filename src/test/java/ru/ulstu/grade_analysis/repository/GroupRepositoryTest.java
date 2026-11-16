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

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GroupRepositoryTest {

  @Autowired private GroupRepository groupRepository;
  @Autowired private EntityManager em;

  private final Pageable all = PageRequest.of(0, 100, Sort.by("prefix", "course", "subgroup"));

  private Group g1;
  private Group g2;
  private Group g3;

  @BeforeEach
  void setUp() {
    g1 =
        groupRepository.save(
            new Group(
                null, "ПИбд", "1", "1", EducationLevel.BACHELOR, new HashSet<>(), new HashSet<>()));
    g2 =
        groupRepository.save(
            new Group(
                null, "ПИбд", "2", "1", EducationLevel.BACHELOR, new HashSet<>(), new HashSet<>()));
    g3 =
        groupRepository.save(
            new Group(
                null, "ИВТм", "1", "1", EducationLevel.MASTER, new HashSet<>(), new HashSet<>()));
    em.flush();
  }

  @Test
  void findFiltered_allNull_returnsAll() {
    Page<Group> page = groupRepository.findFiltered(null, null, null, all);
    assertThat(page.getContent()).containsExactlyInAnyOrder(g1, g2, g3);
    assertThat(page.getTotalElements()).isEqualTo(3);
  }

  @Test
  void findFiltered_byCourse() {
    Page<Group> page = groupRepository.findFiltered("1", null, null, all);
    assertThat(page.getContent()).containsExactlyInAnyOrder(g1, g3);
  }

  @Test
  void findFiltered_byLevel() {
    Page<Group> page = groupRepository.findFiltered(null, EducationLevel.BACHELOR, null, all);
    assertThat(page.getContent()).containsExactlyInAnyOrder(g1, g2);
  }

  @Test
  void findFiltered_bySearch_exactMatch() {
    Page<Group> page = groupRepository.findFiltered(null, null, "ПИбд-11", all);
    assertThat(page.getContent()).containsExactly(g1);
  }

  @Test
  void findFiltered_bySearch_caseInsensitive() {
    Page<Group> page = groupRepository.findFiltered(null, null, "пИБД", all);
    assertThat(page.getContent()).containsExactlyInAnyOrder(g1, g2);
  }

  @Test
  void findFiltered_byCourseAndLevel() {
    Page<Group> page = groupRepository.findFiltered("1", EducationLevel.BACHELOR, null, all);
    assertThat(page.getContent()).containsExactly(g1);
  }

  @Test
  void findFiltered_allThreeFilters() {
    Page<Group> page = groupRepository.findFiltered("1", EducationLevel.MASTER, "ИВТм", all);
    assertThat(page.getContent()).containsExactly(g3);
  }

  @Test
  void findFiltered_noMatches_empty() {
    Page<Group> page = groupRepository.findFiltered(null, null, "Несуществующая", all);
    assertThat(page.getContent()).isEmpty();
  }

  @Test
  void findFiltered_pagination() {
    Sort sort = Sort.by("prefix", "course", "subgroup");
    Page<Group> page0 = groupRepository.findFiltered(null, null, null, PageRequest.of(0, 2, sort));
    Page<Group> page1 = groupRepository.findFiltered(null, null, null, PageRequest.of(1, 2, sort));

    assertThat(page0.getContent()).hasSize(2);
    assertThat(page0.getTotalElements()).isEqualTo(3);
    assertThat(page0.getTotalPages()).isEqualTo(2);
    assertThat(page1.getContent()).hasSize(1);
  }
}
