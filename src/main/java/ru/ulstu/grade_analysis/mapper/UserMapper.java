package ru.ulstu.grade_analysis.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.ulstu.grade_analysis.dto.GroupResponse;
import ru.ulstu.grade_analysis.dto.StudentResponse;
import ru.ulstu.grade_analysis.dto.UserResponse;
import ru.ulstu.grade_analysis.entity.Group;
import ru.ulstu.grade_analysis.entity.User;

@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    List<String> groupNames = new ArrayList<>();
    List<Long> groupIds = new ArrayList<>();

    String roleName = user.getRole() != null ? user.getRole().getName() : "";
    if ("STUDENT".equals(roleName)) {
      collectGroups(user.getStudentGroups(), groupNames, groupIds);
    } else if ("TEACHER".equals(roleName)) {
      collectGroups(user.getTeacherGroups(), groupNames, groupIds);
    }

    return new UserResponse(
        user.getId(),
        user.getFullName(),
        user.getUsername(),
        user.getCourse(),
        user.getRecordBookNumber(),
        user.getEducationForm(),
        user.getEducationLevel(),
        groupNames,
        groupIds);
  }

  public StudentResponse toStudentResponse(User user) {
    return new StudentResponse(
        user.getId(),
        user.getFullName(),
        user.getUsername(),
        user.getCourse(),
        user.getRecordBookNumber());
  }

  public GroupResponse.StudentShortDto toStudentShort(User user) {
    return new GroupResponse.StudentShortDto(user.getId(), user.getFullName());
  }

  private void collectGroups(Iterable<Group> groups, List<String> groupNames, List<Long> groupIds) {
    for (Group group : groups) {
      groupNames.add(group.getFullName());
      groupIds.add(group.getId());
    }
  }
}
