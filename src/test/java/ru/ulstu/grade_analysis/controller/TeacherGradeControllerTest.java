package ru.ulstu.grade_analysis.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GradeRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherGradeControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private SubjectRepository subjectRepository;
  @MockitoBean private GradeRepository gradeRepository;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  @Test
  void getMySubjects_shouldReturnList() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    Subject s1 = new Subject();
    s1.setId(1L);
    s1.setName("БД");
    s1.setTeachers(Set.of(t));
    Subject s2 = new Subject();
    s2.setId(2L);
    s2.setName("БЖД");
    s2.setTeachers(Set.of(t));

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));
    when(subjectRepository.findAll()).thenReturn(List.of(s1, s2));

    mockMvc
        .perform(
            get("/api/teacher/grades/my-subjects").with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void getGradesBySubject_shouldReturnList() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));
    when(gradeRepository.findBySubjectIdAndTeacherId(1L, 1L)).thenReturn(new ArrayList<>());

    mockMvc
        .perform(
            get("/api/teacher/grades/subject/1").with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isOk());
  }

  @Test
  void saveGrade_shouldReturnOk() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    User st = new User();
    st.setId(10L);
    Subject s = new Subject();
    s.setId(1L);

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));
    when(userRepository.findById(10L)).thenReturn(Optional.of(st));
    when(subjectRepository.findById(1L)).thenReturn(Optional.of(s));
    when(gradeRepository.findByStudentIdAndSubjectIdAndTeacherId(anyLong(), anyLong(), anyLong()))
        .thenReturn(Optional.empty());

    String body = "{\"studentId\":10,\"subjectId\":1,\"value\":5}";
    mockMvc
        .perform(
            post("/api/teacher/grades")
                .with(authentication(auth("teacher", "TEACHER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Оценка сохранена"));
  }

  @Test
  void saveGrade_shouldFail_whenValueOutOfRange() throws Exception {
    String body = "{\"studentId\":10,\"subjectId\":1,\"value\":7}";
    mockMvc
        .perform(
            post("/api/teacher/grades")
                .with(authentication(auth("teacher", "TEACHER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getSubjectDetails_shouldReturn403_whenNoAccess() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    Subject s = new Subject();
    s.setId(1L);
    s.setTeachers(new HashSet<>());

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));
    when(subjectRepository.findById(1L)).thenReturn(Optional.of(s));

    mockMvc
        .perform(
            get("/api/teacher/grades/subject/1/details")
                .with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isForbidden());
  }
}
