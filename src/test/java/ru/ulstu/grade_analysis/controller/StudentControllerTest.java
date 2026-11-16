package ru.ulstu.grade_analysis.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
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
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private GradeRepository gradeRepository;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  @Test
  void getMyGrades_shouldReturnStats() throws Exception {
    User st = new User();
    st.setId(1L);
    st.setUsername("student");
    st.setLastName("Иванов");
    st.setFirstName("Иван");
    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    st.setStudentGroups(Set.of(g));

    Subject s = new Subject();
    s.setName("БД");
    Grade gr1 = new Grade();
    gr1.setValue(5);
    gr1.setSubject(s);
    Grade gr2 = new Grade();
    gr2.setValue(3);
    gr2.setSubject(s);

    when(userRepository.findByUsername("student")).thenReturn(Optional.of(st));
    when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(gr1, gr2));

    mockMvc
        .perform(get("/api/student/my-grades").with(authentication(auth("student", "STUDENT"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.average").value(4.0))
        .andExpect(jsonPath("$.groupName").value("ПИбд-11"));
  }

  @Test
  void getMyGrades_shouldReturnZeroDebts_whenNoBadGrades() throws Exception {
    User st = new User();
    st.setId(1L);
    st.setUsername("student");
    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    st.setStudentGroups(Set.of(g));
    Subject s = new Subject();
    s.setName("БД");
    Grade gr1 = new Grade();
    gr1.setValue(5);
    gr1.setSubject(s);

    when(userRepository.findByUsername("student")).thenReturn(Optional.of(st));
    when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(gr1));

    mockMvc
        .perform(get("/api/student/my-grades").with(authentication(auth("student", "STUDENT"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.debts.length()").value(0));
  }

  @Test
  void getMyGrades_shouldReturnDebts_whenBadGrades() throws Exception {
    User st = new User();
    st.setId(1L);
    st.setUsername("student");
    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    st.setStudentGroups(Set.of(g));

    Subject s1 = new Subject();
    s1.setName("БД");
    Subject s2 = new Subject();
    s2.setName("БЖД");
    Grade gr1 = new Grade();
    gr1.setValue(2);
    gr1.setSubject(s1);
    Grade gr2 = new Grade();
    gr2.setValue(0);
    gr2.setSubject(s2);

    when(userRepository.findByUsername("student")).thenReturn(Optional.of(st));
    when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(gr1, gr2));

    mockMvc
        .perform(get("/api/student/my-grades").with(authentication(auth("student", "STUDENT"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.debts.length()").value(2));
  }

  @Test
  void getMyGrades_shouldHandleEmptyGrades() throws Exception {
    User st = new User();
    st.setId(1L);
    st.setUsername("student");
    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    st.setStudentGroups(Set.of(g));

    when(userRepository.findByUsername("student")).thenReturn(Optional.of(st));
    when(gradeRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/student/my-grades").with(authentication(auth("student", "STUDENT"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.average").value(0.0));
  }
}
