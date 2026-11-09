package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.grade_analysis.dto.SubjectRequest;
import ru.ulstu.grade_analysis.dto.SubjectResponse;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/subjects")
@PreAuthorize("hasRole('ADMIN')")
public class SubjectController {

  private final SubjectRepository subjectRepository;
  private final UserRepository userRepository;

  public SubjectController(SubjectRepository subjectRepository, UserRepository userRepository) {
    this.subjectRepository = subjectRepository;
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<SubjectResponse> getAllSubjects() {
    return subjectRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<SubjectResponse> createSubject(@RequestBody @Valid SubjectRequest request) {
    if (subjectRepository.existsByName(request.name())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Предмет с таким названием уже существует");
    }
    Subject subject = new Subject();
    subject.setName(request.name());
    Subject saved = subjectRepository.save(subject);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  @PutMapping("/{id}")
  public ResponseEntity<SubjectResponse> updateSubject(
      @PathVariable Long id, @RequestBody @Valid SubjectRequest request) {
    if (subjectRepository.existsByNameAndIdNot(request.name(), id)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Предмет с таким названием уже существует");
    }
    Subject subject = subjectRepository.findById(id).orElseThrow();
    subject.setName(request.name());
    Subject saved = subjectRepository.save(subject);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
    subjectRepository.deleteById(id);
    return ResponseEntity.ok(Map.of("message", "Предмет удален"));
  }

  @PostMapping("/{id}/teachers")
  public ResponseEntity<SubjectResponse> assignTeachers(
      @PathVariable Long id, @RequestBody List<Long> teacherIds) {
    Subject subject = subjectRepository.findById(id).orElseThrow();
    List<User> teachers = userRepository.findAllById(teacherIds);
    subject.getTeachers().clear();
    subject.getTeachers().addAll(teachers);
    Subject saved = subjectRepository.save(subject);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  private SubjectResponse mapToResponse(Subject subject) {
    List<SubjectResponse.TeacherShortDto> teacherDtos =
        subject.getTeachers().stream()
            .map(t -> new SubjectResponse.TeacherShortDto(t.getId(), t.getFullName()))
            .collect(Collectors.toList());
    return new SubjectResponse(subject.getId(), subject.getName(), teacherDtos.size(), teacherDtos);
  }
}
