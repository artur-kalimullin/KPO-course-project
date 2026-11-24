package ru.ulstu.grade_analysis.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.RatingResponse;
import ru.ulstu.grade_analysis.service.StudentRatingService;

@RestController
@RequestMapping("/api/student/rating")
@PreAuthorize("hasRole('STUDENT')")
public class StudentRatingController {

  private final StudentRatingService studentRatingService;

  public StudentRatingController(StudentRatingService studentRatingService) {
    this.studentRatingService = studentRatingService;
  }

  @GetMapping
  public RatingResponse getGroupRating(Authentication authentication) {
    return studentRatingService.getGroupRating(authentication.getName());
  }
}
