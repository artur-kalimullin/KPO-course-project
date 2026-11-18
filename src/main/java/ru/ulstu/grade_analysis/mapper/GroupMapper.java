package ru.ulstu.grade_analysis.mapper;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.User;

@Component
public class GroupMapper {
  public GroupResponse toResponse(Group group) {
    List<GroupResponse.StudentShortDto> students =
        group.getStudents().stream()
            .sorted(Comparator.comparing(User::getFullName))
            .map(s -> new GroupResponse.StudentShortDto(s.getId(), s.getFullName()))
            .toList();

    return build(group, students.size(), students);
  }

  public GroupResponse toSummary(Group group) {
    return build(group, group.getStudents().size(), null);
  }

  private GroupResponse build(
      Group group, int studentsCount, List<GroupResponse.StudentShortDto> students) {
    return new GroupResponse(
        group.getId(),
        group.getPrefix(),
        group.getCourse(),
        group.getSubgroup(),
        group.getEducationLevel(),
        group.getFullName(),
        studentsCount,
        students);
  }
}
