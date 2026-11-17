package ru.ulstu.grade_analysis.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.grade_analysis.dto.LoginRequest;
import ru.ulstu.grade_analysis.dto.LoginResponse;
import ru.ulstu.grade_analysis.dto.MessageResponse;
import ru.ulstu.grade_analysis.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<MessageResponse> logout() {
    return ResponseEntity.ok(new MessageResponse("Выход выполнен."));
  }
}
