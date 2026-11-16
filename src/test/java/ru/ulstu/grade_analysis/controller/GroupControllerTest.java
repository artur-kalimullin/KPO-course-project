package ru.ulstu.grade_analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;
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
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.repository.GroupRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private GroupRepository groupRepository;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  @Test
  void createGroup_shouldSuccess_whenBachelor() throws Exception {
    Group g = new Group();
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    g.setEducationLevel(Group.EducationLevel.BACHELOR);
    g.setStudents(new HashSet<>());
    when(groupRepository.save(any())).thenReturn(g);

    String body =
        "{\"prefix\":\"ПИбд\",\"course\":\"1\",\"subgroup\":\"1\",\"educationLevel\":\"BACHELOR\"}";
    mockMvc
        .perform(
            post("/api/admin/groups")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("ПИбд-11"));
  }

  @Test
  void createGroup_shouldFail_whenMasterCourse3() throws Exception {
    String body =
        "{\"prefix\":\"ПИбд\",\"course\":\"3\",\"subgroup\":\"1\",\"educationLevel\":\"MASTER\"}";
    mockMvc
        .perform(
            post("/api/admin/groups")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Для магистратуры доступны только 1 и 2 курсы"));
  }

  @Test
  void createGroup_shouldFail_whenCourseOutOfRange() throws Exception {
    String body =
        "{\"prefix\":\"ПИбд\",\"course\":\"6\",\"subgroup\":\"1\",\"educationLevel\":\"BACHELOR\"}";
    mockMvc
        .perform(
            post("/api/admin/groups")
                .with(authentication(auth("admin", "ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteGroup_shouldReturnOk_whenExists() throws Exception {
    when(groupRepository.existsById(1L)).thenReturn(true);
    mockMvc
        .perform(delete("/api/admin/groups/1").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Группа удалена"));
  }

  @Test
  void deleteGroup_shouldReturn404_whenNotFound() throws Exception {
    when(groupRepository.existsById(99L)).thenReturn(false);
    mockMvc
        .perform(delete("/api/admin/groups/99").with(authentication(auth("admin", "ADMIN"))))
        .andExpect(status().isNotFound());
  }
}
