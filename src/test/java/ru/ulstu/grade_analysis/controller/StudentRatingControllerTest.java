package ru.ulstu.grade_analysis.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ulstu.grade_analysis.entity.Grade;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentRatingControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private GradeRepository gradeRepository;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  @Test
  void getRating_shouldReturnTop3AndStats() throws Exception {
    Role r = new Role();
    r.setName("STUDENT");
    User st1 = new User();
    st1.setId(1L);
    st1.setUsername("student1");
    st1.setRole(r);
    User st2 = new User();
    st2.setId(2L);
    st2.setUsername("student2");
    st2.setRole(r);

    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    g.setStudents(new HashSet<>(Arrays.asList(st1, st2)));
    st1.setStudentGroups(Set.of(g));
    st2.setStudentGroups(Set.of(g));

    Grade gr1 = new Grade();
    gr1.setValue(5);
    Grade gr2 = new Grade();
    gr2.setValue(2);

    when(userRepository.findByUsername("student1")).thenReturn(Optional.of(st1));
    when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(gr1));
    when(gradeRepository.findByStudentId(2L)).thenReturn(List.of(gr2));

    mockMvc
        .perform(get("/api/student/rating").with(authentication(auth("student1", "STUDENT"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.top3.length()").value(2))
        .andExpect(jsonPath("$.myPlace").value(1))
        .andExpect(jsonPath("$.totalStudents").value(2));
  }

  @Test
  void getRating_shouldReturnDebtsStats() throws Exception {
    Role r = new Role();
    r.setName("STUDENT");
    User st1 = new User();
    st1.setId(1L);
    st1.setUsername("student1");
    st1.setRole(r);
    User st2 = new User();
    st2.setId(2L);
    st2.setUsername("student2");
    st2.setRole(r);

    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    g.setStudents(new HashSet<>(Arrays.asList(st1, st2)));
    st1.setStudentGroups(Set.of(g));
    st2.setStudentGroups(Set.of(g));

    Grade gr1 = new Grade();
    gr1.setValue(5);
    Grade gr2 = new Grade();
    gr2.setValue(2);

    when(userRepository.findByUsername("student1")).thenReturn(Optional.of(st1));
    when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(gr1));
    when(gradeRepository.findByStudentId(2L)).thenReturn(List.of(gr2));

    mockMvc
        .perform(get("/api/student/rating").with(authentication(auth("student1", "STUDENT"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studentsWithDebts").value(1))
        .andExpect(jsonPath("$.studentsWithoutDebts").value(1));
  }

  @Test
  void getRating_shouldReturnEmptyTop3_whenNoStudents() throws Exception {
    User st1 = new User();
    st1.setId(1L);
    st1.setUsername("student1");
    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    g.setStudents(new HashSet<>());
    st1.setStudentGroups(Set.of(g));
    when(userRepository.findByUsername("student1")).thenReturn(Optional.of(st1));

    mockMvc
        .perform(get("/api/student/rating").with(authentication(auth("student1", "STUDENT"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.top3.length()").value(0));
  }

  @Test
  void getRating_shouldHandleStudentWithoutGroup() throws Exception {
    User st1 = new User();
    st1.setId(1L);
    st1.setUsername("student1");
    st1.setStudentGroups(new HashSet<>());
    when(userRepository.findByUsername("student1")).thenReturn(Optional.of(st1));

    mockMvc
        .perform(get("/api/student/rating").with(authentication(auth("student1", "STUDENT"))))
        .andExpect(status().is5xxServerError());
  }
}
