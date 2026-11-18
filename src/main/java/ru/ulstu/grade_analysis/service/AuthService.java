package ru.ulstu.grade_analysis.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.ulstu.grade_analysis.dto.LoginRequest;
import ru.ulstu.grade_analysis.dto.LoginResponse;
import ru.ulstu.grade_analysis.security.JwtService;

@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public LoginResponse login(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String token = jwtService.generateToken(userDetails);
    String role = userDetails.getAuthorities().iterator().next().getAuthority();

    return new LoginResponse(token, role, userDetails.getUsername());
  }
}
