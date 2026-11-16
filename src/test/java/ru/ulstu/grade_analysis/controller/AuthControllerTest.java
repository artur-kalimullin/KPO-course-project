package ru.ulstu.grade_analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.ulstu.grade_analysis.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private JwtService jwtService;

  @Test
  void login_shouldReturnTokenAndRole_whenCredentialsAreValid() throws Exception {
    UserDetails userDetails =
        new User(
            "admin", "pass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    Authentication auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    when(authenticationManager.authenticate(any())).thenReturn(auth);
    when(jwtService.generateToken(any())).thenReturn("mock-token");

    String body = "{\"username\":\"admin\",\"password\":\"pass\"}";
    mockMvc
        .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("mock-token"))
        .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
        .andExpect(jsonPath("$.username").value("admin"));
  }

  @Test
  void login_shouldReturn401_whenInvalidCredentials() throws Exception {
    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad"));
    String body = "{\"username\":\"admin\",\"password\":\"wrong\"}";
    mockMvc
        .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Неверный логин или пароль"));
  }

  @Test
  void login_shouldReturn400_whenBlankUsername() throws Exception {
    String body = "{\"username\":\"\",\"password\":\"pass\"}";
    mockMvc
        .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_shouldReturn400_whenBlankPassword() throws Exception {
    String body = "{\"username\":\"admin\",\"password\":\"\"}";
    mockMvc
        .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void logout_shouldReturnMessage() throws Exception {
    mockMvc
        .perform(post("/api/auth/logout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Выход выполнен."));
  }
}
