package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.grade_analysis.dto.SubjectRequest;
import ru.ulstu.grade_analysis.dto.SubjectResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.GroupRepository;
import ru.ulstu.grade_analysis.repository.SubjectRepository;
import ru.ulstu.grade_analysis.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/subjects")
@PreAuthorize("hasRole('ADMIN')")
public class SubjectController {

  private final SubjectRepository subjectRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public SubjectController(
      SubjectRepository subjectRepository,
      UserRepository userRepository,
      GroupRepository groupRepository) {
    this.subjectRepository = subjectRepository;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
  }

  @GetMapping
  public Page<SubjectResponse> getAllSubjects(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Long teacherId,
      @RequestParam(required = false) Long groupId) {

    PageRequest pageRequest = PageRequest.of(page - 1, 16, Sort.by("name"));
    return subjectRepository
        .findFiltered(
            (search != null && !search.isEmpty()) ? search : null, teacherId, groupId, pageRequest)
        .map(this::mapToResponse);
  }

  @PostMapping
  public ResponseEntity<SubjectResponse> createSubject(@RequestBody @Valid SubjectRequest request) {
    if (subjectRepository.existsByName(request.name())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Предмет с таким названием уже существует");
    }
    Subject subject = new Subject();
    subject.setName(request.name());
    updateRelations(subject, request);
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
    Subject subject =
        subjectRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Предмет не найден"));

    subject.setName(request.name());
    updateRelations(subject, request);
    Subject saved = subjectRepository.save(subject);
    return ResponseEntity.ok(mapToResponse(saved));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
    subjectRepository.deleteById(id);
    return ResponseEntity.ok(Map.of("message", "Предмет удален"));
  }

  private void updateRelations(Subject subject, SubjectRequest request) {
    subject.getTeachers().clear();
    if (request.teacherIds() != null && !request.teacherIds().isEmpty()) {
      List<User> teachers = userRepository.findAllById(request.teacherIds());
      subject.getTeachers().addAll(teachers);
    }

    subject.getGroups().clear();
    if (request.groupIds() != null && !request.groupIds().isEmpty()) {
      List<Group> groups = groupRepository.findAllById(request.groupIds());
      subject.getGroups().addAll(groups);
    }
  }

  private SubjectResponse mapToResponse(Subject subject) {
    List<SubjectResponse.TeacherShortDto> teacherDtos =
        subject.getTeachers().stream()
            .map(t -> new SubjectResponse.TeacherShortDto(t.getId(), t.getFullName()))
            .collect(Collectors.toList());

    List<SubjectResponse.GroupShortDto> groupDtos =
        subject.getGroups().stream()
            .map(g -> new SubjectResponse.GroupShortDto(g.getId(), g.getFullName()))
            .collect(Collectors.toList());

    return new SubjectResponse(
        subject.getId(), subject.getName(), teacherDtos.size(), teacherDtos, groupDtos);
  }
}
