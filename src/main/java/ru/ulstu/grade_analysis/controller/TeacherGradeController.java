package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.GradeDto.GradeRequest;
import ru.ulstu.grade_analysis.dto.GradeDto.GradeResponse;
import ru.ulstu.grade_analysis.dto.MessageResponse;
import ru.ulstu.grade_analysis.dto.SubjectDetailsResponse;
import ru.ulstu.grade_analysis.dto.SubjectOption;
import ru.ulstu.grade_analysis.service.GradeService;

@RestController
@RequestMapping("/api/teacher/grades")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherGradeController {

  private final GradeService gradeService;

  public TeacherGradeController(GradeService gradeService) {
    this.gradeService = gradeService;
  }

  @GetMapping("/my-subjects")
  public List<SubjectOption> getMySubjects(Authentication authentication) {
    return gradeService.findMySubjects(authentication.getName());
  }

  @GetMapping("/subject/{subjectId}")
  public List<GradeResponse> getGradesBySubject(
      @PathVariable Long subjectId, Authentication authentication) {
    return gradeService.findGradesBySubject(authentication.getName(), subjectId);
  }

  @GetMapping("/subject/{subjectId}/details")
  public SubjectDetailsResponse getSubjectDetails(
      @PathVariable Long subjectId, Authentication authentication) {
    return gradeService.getSubjectDetails(authentication.getName(), subjectId);
  }

  @PostMapping
  public ResponseEntity<MessageResponse> saveGrade(
      @RequestBody @Valid GradeRequest request, Authentication authentication) {
    gradeService.saveGrade(authentication.getName(), request);
    return ResponseEntity.ok(new MessageResponse("Оценка сохранена"));
  }
}
