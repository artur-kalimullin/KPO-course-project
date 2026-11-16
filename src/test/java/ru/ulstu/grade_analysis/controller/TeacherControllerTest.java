package ru.ulstu.grade_analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private GroupRepository groupRepository;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private SubjectRepository subjectRepository;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  @Test
  void getMyGroups_shouldReturnList() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    Group g = new Group();
    g.setId(10L);
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    g.setEducationLevel(Group.EducationLevel.BACHELOR);
    g.setStudents(new HashSet<>());
    t.setTeacherGroups(Set.of(g));

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));

    mockMvc
        .perform(get("/api/teacher/my-groups").with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].fullName").value("ПИбд-11"));
  }

  @Test
  void getSubjectsByGroup_shouldReturnFilteredList() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    Group g = new Group();
    g.setId(10L);
    Subject s1 = new Subject();
    s1.setId(1L);
    s1.setName("БД");
    s1.setTeachers(Set.of(t));
    s1.setGroups(Set.of(g));
    Subject s2 = new Subject();
    s2.setId(2L);
    s2.setName("БЖД");
    s2.setTeachers(Set.of(t));
    s2.setGroups(new HashSet<>()); // не привязан к группе 10

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));
    when(subjectRepository.findAll()).thenReturn(List.of(s1, s2));

    mockMvc
        .perform(
            get("/api/teacher/groups/10/subjects").with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("БД"));
  }

  @Test
  void getStudentsByGroup_shouldReturnPage_whenHasAccess() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    Group g = new Group();
    g.setId(10L);
    t.setTeacherGroups(Set.of(g));

    User st = new User();
    st.setId(2L);
    st.setUsername("student");
    st.setLastName("Иванов");
    st.setFirstName("Иван");
    st.setCourse("1");
    st.setRecordBookNumber("123456");

    Page<User> page = new PageImpl<>(List.of(st));

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));
    when(userRepository.findStudentsInGroup(any(), any(), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/api/teacher/groups/10/students").with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].fullName").value("Иванов Иван"));
  }

  @Test
  void getStudentsByGroup_shouldReturn403_whenNoAccess() throws Exception {
    User t = new User();
    t.setUsername("teacher");
    t.setId(1L);
    t.setTeacherGroups(new HashSet<>());

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(t));

    mockMvc
        .perform(
            get("/api/teacher/groups/10/students").with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isForbidden());
  }
}
