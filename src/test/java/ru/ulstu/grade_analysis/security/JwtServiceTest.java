package ru.ulstu.grade_analysis.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private static final String SECRET = "testSecretKeyForJWTTokenGenerationMustBeLongEnough123456";

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    ReflectionTestUtils.setField(jwtService, "expiration", 3_600_000L);
  }

  private UserDetails user(String name) {
    return new User(name, "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  @Test
  void generateToken_and_extractUsername() {
    String token = jwtService.generateToken(user("alice"));
    assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
  }

  @Test
  void isTokenValid_true_forMatchingUser() {
    UserDetails alice = user("alice");
    String token = jwtService.generateToken(alice);
    assertThat(jwtService.isTokenValid(token, alice)).isTrue();
  }

  @Test
  void isTokenValid_false_forDifferentUser() {
    String token = jwtService.generateToken(user("alice"));
    assertThat(jwtService.isTokenValid(token, user("bob"))).isFalse();
  }

  @Test
  void isTokenValid_throwsExpiredJwtException_forExpiredToken() throws InterruptedException {
    ReflectionTestUtils.setField(jwtService, "expiration", 1L);
    UserDetails alice = user("alice");
    String token = jwtService.generateToken(alice);
    Thread.sleep(20);
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> jwtService.isTokenValid(token, alice))
        .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
  }
}
