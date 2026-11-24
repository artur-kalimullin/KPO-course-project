package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.MessageResponse;
import ru.ulstu.grade_analysis.dto.SubjectRequest;
import ru.ulstu.grade_analysis.dto.SubjectResponse;
import ru.ulstu.grade_analysis.service.SubjectService;

@RestController
@RequestMapping("/api/admin/subjects")
@PreAuthorize("hasRole('ADMIN')")
public class SubjectController {

  private final SubjectService subjectService;

  public SubjectController(SubjectService subjectService) {
    this.subjectService = subjectService;
  }

  @GetMapping
  public Page<SubjectResponse> getAllSubjects(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Long teacherId,
      @RequestParam(required = false) Long groupId) {

    return subjectService.findAll(page, search, teacherId, groupId);
  }

  @PostMapping
  public ResponseEntity<SubjectResponse> createSubject(@RequestBody @Valid SubjectRequest request) {
    return ResponseEntity.ok(subjectService.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<SubjectResponse> updateSubject(
      @PathVariable Long id, @RequestBody @Valid SubjectRequest request) {
    return ResponseEntity.ok(subjectService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<MessageResponse> deleteSubject(@PathVariable Long id) {
    subjectService.delete(id);
    return ResponseEntity.ok(new MessageResponse("Предмет удален"));
  }
}
