package ru.ulstu.grade_analysis.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import ru.ulstu.grade_analysis.entity.Role;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

  @Autowired private RoleRepository roleRepository;
  @Autowired private EntityManager em;

  @BeforeEach
  void setUp() {
    roleRepository.save(new Role(null, "ADMIN"));
    roleRepository.save(new Role(null, "STUDENT"));
    em.flush();
  }

  @Test
  void findByName_found() {
    assertThat(roleRepository.findByName("ADMIN"))
        .isPresent()
        .get()
        .extracting(Role::getName)
        .isEqualTo("ADMIN");
  }

  @Test
  void findByName_notFound() {
    assertThat(roleRepository.findByName("UNKNOWN")).isEmpty();
  }

  @Test
  void findByName_caseSensitive() {
    assertThat(roleRepository.findByName("admin")).isEmpty();
  }
}
