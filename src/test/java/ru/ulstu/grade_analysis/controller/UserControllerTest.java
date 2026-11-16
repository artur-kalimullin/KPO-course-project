package ru.ulstu.grade_analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.RoleRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private RoleRepository roleRepository;
  @MockitoBean private GroupRepository groupRepository;
  @MockitoBean private PasswordEncoder passwordEncoder;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  @Test
  void getAllStudents_shouldReturnPage() throws Exception {
    User st = new User();
    st.setId(1L);
    st.setUsername("student");
    st.setLastName("Иванов");
    st.setFirstName("Иван");
    st.setCourse("1");
    st.setRecordBookNumber("123456");
    Role r = new Role();
    r.setName("STUDENT");
    st.setRole(r);

    Page<User> page = new PageImpl<>(List.of(st));
    when(userRepository.findFilteredStudents(any(), any(), any(), any(Pageable.class)))
        .thenReturn(page);

    mockMvc
        .perform(get("/api/admin/users/students").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].fullName").value("Иванов Иван"));
  }

  @Test
  void getAllTeachers_shouldReturnPage() throws Exception {
    Page<User> page = new PageImpl<>(List.of());
    when(userRepository.findFilteredTeachers(any(), any(), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/admin/users/teachers").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  void createStudent_shouldReturnOk() throws Exception {
    Role r = new Role();
    r.setName("STUDENT");
    Group g = new Group();
    g.setId(1L);
    g.setStudents(new HashSet<>());

    when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(r));
    when(groupRepository.findById(1L)).thenReturn(Optional.of(g));
    when(userRepository.findByUsername("student")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("hash");

    String body =
        "{\"username\":\"student\",\"password\":\"pass123\","
            + "\"firstName\":\"Иван\",\"lastName\":\"Иванов\",\"course\":\"1\","
            + "\"groupId\":1,\"recordBookNumber\":\"123456\","
            + "\"educationForm\":\"Очная\",\"educationLevel\":\"Бакалавриат\"}";

    mockMvc
        .perform(
            post("/api/admin/users/students")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Студент добавлен"));
  }

  @Test
  void createStudent_shouldReturn400_whenBlankPassword() throws Exception {
    String body =
        "{\"username\":\"student\",\"password\":\"\","
            + "\"firstName\":\"Иван\",\"lastName\":\"Иванов\",\"course\":\"1\","
            + "\"groupId\":1,\"recordBookNumber\":\"123456\","
            + "\"educationForm\":\"Очная\",\"educationLevel\":\"Бакалавриат\"}";

    mockMvc
        .perform(
            post("/api/admin/users/students")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Пароль не может быть пустым"));
  }

  @Test
  void createStudent_shouldReturn400_whenUsernameTaken() throws Exception {
    when(userRepository.findByUsername("student")).thenReturn(Optional.of(new User()));

    String body =
        "{\"username\":\"student\",\"password\":\"pass123\","
            + "\"firstName\":\"Иван\",\"lastName\":\"Иванов\",\"course\":\"1\","
            + "\"groupId\":1,\"recordBookNumber\":\"123456\","
            + "\"educationForm\":\"Очная\",\"educationLevel\":\"Бакалавриат\"}";

    mockMvc
        .perform(
            post("/api/admin/users/students")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Логин уже занят"));
  }

  @Test
  void deleteUser_shouldReturnOk() throws Exception {
    User u = new User();
    u.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(u));

    mockMvc
        .perform(delete("/api/admin/users/1").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Пользователь удален"));
  }

  @Test
  void deleteUser_shouldReturn404_whenNotFound() throws Exception {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    mockMvc
        .perform(delete("/api/admin/users/99").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isNotFound());
  }
}
