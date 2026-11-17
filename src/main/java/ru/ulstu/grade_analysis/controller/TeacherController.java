package ru.ulstu.grade_analysis.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.dto.StudentResponse;
import ru.ulstu.grade_analysis.dto.SubjectOption;
import ru.ulstu.grade_analysis.service.TeacherService;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

  private final TeacherService teacherService;

  public TeacherController(TeacherService teacherService) {
    this.teacherService = teacherService;
  }

  @GetMapping("/my-groups")
  public List<GroupResponse> getMyGroups(Authentication authentication) {
    return teacherService.findMyGroups(authentication.getName());
  }

  @GetMapping("/groups/{groupId}/subjects")
  public List<SubjectOption> getSubjectsByGroup(
      @PathVariable Long groupId, Authentication authentication) {
    return teacherService.findSubjectsByGroup(authentication.getName(), groupId);
  }

  @GetMapping("/groups/{groupId}/students")
  public Page<StudentResponse> getStudentsByGroup(
      @PathVariable Long groupId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String search,
      Authentication authentication) {

    return teacherService.findStudentsByGroup(authentication.getName(), groupId, page, search);
  }
}
