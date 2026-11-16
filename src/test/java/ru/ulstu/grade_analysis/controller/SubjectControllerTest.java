package ru.ulstu.grade_analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubjectControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private SubjectRepository subjectRepository;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private GroupRepository groupRepository;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  private static Subject subject(Long id, String name) {
    Subject s = new Subject();
    s.setId(id);
    s.setName(name);
    s.setTeachers(new HashSet<>());
    s.setGroups(new HashSet<>());
    return s;
  }

  @Test
  void getAllSubjects_shouldReturnPage() throws Exception {
    Page<Subject> page = new PageImpl<>(List.of(subject(1L, "БД")));
    when(subjectRepository.findFiltered(any(), any(), any(), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/admin/subjects").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("БД"));
  }

  @Test
  void createSubject_shouldReturnOk_whenNameUnique() throws Exception {
    when(subjectRepository.existsByName("БД")).thenReturn(false);
    when(subjectRepository.save(any()))
        .thenAnswer(
            inv -> {
              Subject s = inv.getArgument(0);
              s.setId(1L);
              return s;
            });

    String body = "{\"name\":\"БД\",\"teacherIds\":[],\"groupIds\":[]}";
    mockMvc
        .perform(
            post("/api/admin/subjects")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("БД"));
  }

  @Test
  void createSubject_shouldReturn400_whenNameDuplicate() throws Exception {
    when(subjectRepository.existsByName("БД")).thenReturn(true);

    String body = "{\"name\":\"БД\"}";
    mockMvc
        .perform(
            post("/api/admin/subjects")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Предмет с таким названием уже существует"));
  }

  @Test
  void updateSubject_shouldReturnOk() throws Exception {
    when(subjectRepository.existsByNameAndIdNot("БД", 1L)).thenReturn(false);
    when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject(1L, "Старое")));
    when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    String body = "{\"name\":\"БД\"}";
    mockMvc
        .perform(
            put("/api/admin/subjects/1")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("БД"));
  }

  @Test
  void updateSubject_shouldReturn400_whenNameDuplicate() throws Exception {
    when(subjectRepository.existsByNameAndIdNot("БД", 1L)).thenReturn(true);

    String body = "{\"name\":\"БД\"}";
    mockMvc
        .perform(
            put("/api/admin/subjects/1")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateSubject_shouldReturn404_whenNotFound() throws Exception {
    when(subjectRepository.existsByNameAndIdNot("БД", 99L)).thenReturn(false);
    when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

    String body = "{\"name\":\"БД\"}";
    mockMvc
        .perform(
            put("/api/admin/subjects/99")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteSubject_shouldReturnOk() throws Exception {
    mockMvc
        .perform(delete("/api/admin/subjects/1").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Предмет удален"));
  }

  @Test
  void getAllSubjects_shouldReturn403_whenNotAdmin() throws Exception {
    mockMvc
        .perform(get("/api/admin/subjects").with(authentication(auth("student", "STUDENT"))))
        .andExpect(status().isForbidden());
  }
}
