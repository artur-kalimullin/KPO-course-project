package ru.ulstu.grade_analysis.mapper;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.ulstu.grade_analysis.dto.SubjectOption;
import ru.ulstu.grade_analysis.dto.SubjectResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.Subject;
import ru.ulstu.grade_analysis.entity.User;

@Component
public class SubjectMapper {

  public SubjectResponse toResponse(Subject subject) {
    List<SubjectResponse.TeacherShortDto> teachers =
        subject.getTeachers().stream()
            .sorted(Comparator.comparing(User::getFullName))
            .map(t -> new SubjectResponse.TeacherShortDto(t.getId(), t.getFullName()))
            .toList();

    List<SubjectResponse.GroupShortDto> groups =
        subject.getGroups().stream()
            .sorted(Comparator.comparing(Group::getFullName))
            .map(g -> new SubjectResponse.GroupShortDto(g.getId(), g.getFullName()))
            .toList();

    return new SubjectResponse(
        subject.getId(), subject.getName(), teachers.size(), teachers, groups);
  }

  public SubjectOption toOption(Subject subject) {
    return new SubjectOption(subject.getId(), subject.getName());
  }
}
