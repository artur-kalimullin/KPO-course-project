package ru.ulstu.grade_analysis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.ulstu.grade_analysis.security.JwtAuthenticationFilter;

class SecurityConfigTest {

  private final SecurityConfig config =
      new SecurityConfig(mock(JwtAuthenticationFilter.class), mock(UserDetailsService.class));

  @Test
  void passwordEncoder_isBCrypt_andEncodesCorrectly() {
    PasswordEncoder encoder = config.passwordEncoder();
    assertThat(encoder).isNotNull();
    String hash = encoder.encode("secret");
    assertThat(hash).startsWith("$2");
    assertThat(encoder.matches("secret", hash)).isTrue();
    assertThat(encoder.matches("wrong", hash)).isFalse();
  }

  @Test
  void authenticationProvider_isConfiguredWithEncoder() {
    assertThat(config.authenticationProvider()).isNotNull();
  }
}
