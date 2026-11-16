package ru.ulstu.grade_analysis.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.ulstu.grade_analysis.entity.Role;
import ru.ulstu.grade_analysis.entity.User;
import ru.ulstu.grade_analysis.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserDetailsServiceImpl service;

  @Test
  void loadUserByUsername_success() {
    Role role = new Role();
    role.setName("ADMIN");
    User user = new User();
    user.setUsername("admin");
    user.setPasswordHash("hash");
    user.setRole(role);

    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

    UserDetails ud = service.loadUserByUsername("admin");

    assertThat(ud.getUsername()).isEqualTo("admin");
    assertThat(ud.getPassword()).isEqualTo("hash");
    assertThat(ud.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_ADMIN");
  }

  @Test
  void loadUserByUsername_throws_whenNotFound() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("ghost");
  }
}
