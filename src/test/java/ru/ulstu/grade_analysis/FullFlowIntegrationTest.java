package ru.ulstu.grade_analysis;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.RoleRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FullFlowIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    Role adminRole =
        roleRepository
            .findByName("ADMIN")
            .orElseGet(() -> roleRepository.save(new Role(null, "ADMIN")));
    roleRepository
        .findByName("TEACHER")
        .orElseGet(() -> roleRepository.save(new Role(null, "TEACHER")));
    roleRepository
        .findByName("STUDENT")
        .orElseGet(() -> roleRepository.save(new Role(null, "STUDENT")));

    if (userRepository.findByUsername("testadmin").isEmpty()) {
      User admin = new User();
      admin.setUsername("testadmin");
      admin.setPasswordHash(passwordEncoder.encode("adminpass"));
      admin.setLastName("Админ");
      admin.setFirstName("Системы");
      admin.setRole(adminRole);
      admin.setStudentGroups(new HashSet<>());
      admin.setTeacherGroups(new HashSet<>());
      userRepository.save(admin);
    }
  }

  private String loginAndGetToken(String username, String password) throws Exception {
    String loginBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
            .andExpect(status().isOk())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
  }

  @Test
  void fullUniversityLifecycleScenario() throws Exception {
    // ==========================================
    // ЭТАП 1: Действия Администратора
    // ==========================================
    String adminToken = loginAndGetToken("testadmin", "adminpass");

    String groupBody =
        "{\"prefix\":\"ПИбд\",\"course\":\"1\",\"subgroup\":\"4\",\"educationLevel\":\"BACHELOR\"}";
    MvcResult groupResult =
        mockMvc
            .perform(
                post("/api/admin/groups")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(groupBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("ПИбд-14"))
            .andReturn();
    long groupId =
        objectMapper.readTree(groupResult.getResponse().getContentAsString()).get("id").asLong();

    String studentBody =
        "{\"username\":\"artur123\",\"password\":\"password123\",\"firstName\":\"Артур\",\"lastName\":\"Калимуллин\",\"middleName\":\"Рамилевич\",\"course\":\"1\",\"groupId\":"
            + groupId
            + ",\"recordBookNumber\":\"123456\",\"educationForm\":\"Очная\",\"educationLevel\":\"Бакалавриат\"}";
    mockMvc
        .perform(
            post("/api/admin/users/students")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(studentBody))
        .andExpect(status().isOk());

    User student = userRepository.findByUsername("artur123").orElseThrow();
    long studentId = student.getId();

    String teacherBody =
        "{\"username\":\"ankilov\",\"password\":\"password123\",\"firstName\":\"Андрей\",\"lastName\":\"Анкилов\",\"middleName\":\"Владимирович\",\"groupIds\":["
            + groupId
            + "]}";
    mockMvc
        .perform(
            post("/api/admin/users/teachers")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(teacherBody))
        .andExpect(status().isOk());

    User teacher = userRepository.findByUsername("ankilov").orElseThrow();
    long teacherId = teacher.getId();

    String subjectBody =
        "{\"name\":\"Высшая математика\",\"teacherIds\":["
            + teacherId
            + "],\"groupIds\":["
            + groupId
            + "]}";

    MvcResult subjectResult =
        mockMvc
            .perform(
                post("/api/admin/subjects")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(subjectBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    long subjectId =
        objectMapper.readTree(subjectResult.getResponse().getContentAsString()).get("id").asLong();

    // ==========================================
    // ЭТАП 2: Действия Преподавателя
    // ==========================================
    String teacherToken = loginAndGetToken("ankilov", "password123");

    String gradeBody =
        "{\"studentId\":" + studentId + ",\"subjectId\":" + subjectId + ",\"value\":5}";
    mockMvc
        .perform(
            post("/api/teacher/grades")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gradeBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Оценка сохранена"));

    // ==========================================
    // ЭТАП 3: Действия Студента
    // ==========================================
    String studentToken = loginAndGetToken("artur123", "password123");

    mockMvc
        .perform(
            get("/api/student/rating")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .header("Authorization", "Bearer " + studentToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupName").value("ПИбд-14"))
        .andExpect(jsonPath("$.myPlace").value(1))
        .andExpect(jsonPath("$.myAverage").value(5.0))
        .andExpect(jsonPath("$.top3[0].fullName").value("Калимуллин Артур Рамилевич"));

    mockMvc
        .perform(
            get("/api/student/my-grades")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .header("Authorization", "Bearer " + studentToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.grades[0].subjectName").value("Высшая математика"))
        .andExpect(jsonPath("$.grades[0].value").value(5))
        .andExpect(jsonPath("$.debts.length()").value(0));
  }
}
