package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.request.RegisterRequest;
import learning.griddynamics.com.storeapi.entity.User;
import learning.griddynamics.com.storeapi.exception.InvalidCredentialsException;
import learning.griddynamics.com.storeapi.exception.UserAlreadyExistsException;
import learning.griddynamics.com.storeapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("john@example.com", "SecurePass123");
    }

    @Test
    void givenNewEmail_whenRegister_thenUserIsSaved() {
        // given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("hashedPassword");

        // when
        userService.register(registerRequest);

        // then
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("john@example.com")
                        && user.getPasswordHash().equals("hashedPassword")));
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowUserAlreadyExistsException() {
        // given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(registerRequest.email());

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenValidCredentials_whenAuthenticate_thenReturnUser() {
        // given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .passwordHash("hashedPassword")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePass123", "hashedPassword")).thenReturn(true);

        // when
        User result = userService.authenticate("john@example.com", "SecurePass123");

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    void givenUnknownEmail_whenAuthenticate_thenThrowInvalidCredentialsException() {
        // given
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.authenticate("unknown@example.com", "anyPassword"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void givenWrongPassword_whenAuthenticate_thenThrowInvalidCredentialsException() {
        // given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .passwordHash("hashedPassword")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> userService.authenticate("john@example.com", "wrongPassword"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}