package ru.ulstu.grade_analysis.controller;

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
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherStatsControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private GroupRepository groupRepository;
  @MockitoBean private SubjectRepository subjectRepository;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private GradeRepository gradeRepository;

  private static Authentication auth(String username, String role) {
    return new UsernamePasswordAuthenticationToken(
        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  @Test
  void getStats_shouldReturnDistributionAndFailedList() throws Exception {
    User teacher = new User();
    teacher.setUsername("teacher");
    teacher.setId(1L);

    User st1 = new User();
    st1.setId(1L);
    st1.setLastName("Иванов");
    st1.setFirstName("Иван");

    User st2 = new User();
    st2.setId(2L);
    st2.setLastName("Петров");
    st2.setFirstName("Пётр");

    User st3 = new User();
    st3.setId(3L);
    st3.setLastName("Сидоров");
    st3.setFirstName("Сидор");

    Group g = new Group();
    g.setId(10L);
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    g.setEducationLevel(Group.EducationLevel.BACHELOR);
    g.setStudents(new HashSet<>(Set.of(st1, st2, st3)));

    Subject s = new Subject();
    s.setId(1L);
    s.setName("БД");

    g.setTeachers(Set.of(teacher));
    teacher.setTeacherGroups(Set.of(g));
    s.setTeachers(Set.of(teacher));
    s.setGroups(Set.of(g));

    Grade gr1 = new Grade();
    gr1.setStudent(st1);
    gr1.setSubject(s);
    gr1.setTeacher(teacher);
    gr1.setValue(5);

    Grade gr2 = new Grade();
    gr2.setStudent(st2);
    gr2.setSubject(s);
    gr2.setTeacher(teacher);
    gr2.setValue(2);

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacher));
    when(groupRepository.findById(10L)).thenReturn(Optional.of(g));
    when(subjectRepository.findById(1L)).thenReturn(Optional.of(s));
    when(gradeRepository.findBySubjectIdAndTeacherId(1L, 1L)).thenReturn(List.of(gr1, gr2));

    mockMvc
        .perform(
            get("/api/teacher/stats?groupId=10&subjectId=1")
                .with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupName").value("ПИбд-11"))
        .andExpect(jsonPath("$.totalStudents").value(3))
        .andExpect(jsonPath("$.certifiedCount").value(1))
        .andExpect(jsonPath("$.failedCount").value(2))
        .andExpect(jsonPath("$.failedStudents.length()").value(2));
  }

  @Test
  void getStats_shouldMarkUngradedAsFailed() throws Exception {
    User teacher = new User();
    teacher.setUsername("teacher");
    teacher.setId(1L);

    User st1 = new User();
    st1.setId(1L);

    Group g = new Group();
    g.setId(10L);
    g.setPrefix("ПИбд");
    g.setCourse("1");
    g.setSubgroup("1");
    g.setEducationLevel(Group.EducationLevel.BACHELOR);
    g.setStudents(new HashSet<>(Set.of(st1)));

    Subject s = new Subject();
    s.setId(1L);

    g.setTeachers(Set.of(teacher));
    teacher.setTeacherGroups(Set.of(g));
    s.setTeachers(Set.of(teacher));
    s.setGroups(Set.of(g));

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacher));
    when(groupRepository.findById(10L)).thenReturn(Optional.of(g));
    when(subjectRepository.findById(1L)).thenReturn(Optional.of(s));
    when(gradeRepository.findBySubjectIdAndTeacherId(1L, 1L)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/teacher/stats?groupId=10&subjectId=1")
                .with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.failedCount").value(1))
        .andExpect(jsonPath("$.failedStudents[0].grade").value(-1));
  }

  @Test
  void getStats_shouldReturn403_whenNoAccess() throws Exception {
    User teacher = new User();
    teacher.setUsername("teacher");
    teacher.setId(1L);

    Group g = new Group();
    g.setId(10L);
    g.setStudents(new HashSet<>());

    Subject s = new Subject();
    s.setId(1L);
    s.setTeachers(Set.of());
    s.setGroups(Set.of());

    when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacher));
    when(groupRepository.findById(10L)).thenReturn(Optional.of(g));
    when(subjectRepository.findById(1L)).thenReturn(Optional.of(s));

    mockMvc
        .perform(
            get("/api/teacher/stats?groupId=10&subjectId=1")
                .with(authentication(auth("teacher", "TEACHER"))))
        .andExpect(status().isForbidden());
  }
}
