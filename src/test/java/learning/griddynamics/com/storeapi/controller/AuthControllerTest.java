package learning.griddynamics.com.storeapi.controller;

import learning.griddynamics.com.storeapi.exception.GlobalExceptionHandler;
import tools.jackson.databind.ObjectMapper;
import learning.griddynamics.com.storeapi.dto.request.LoginRequest;
import learning.griddynamics.com.storeapi.dto.request.RegisterRequest;
import learning.griddynamics.com.storeapi.entity.User;
import learning.griddynamics.com.storeapi.exception.InvalidCredentialsException;
import learning.griddynamics.com.storeapi.exception.UserAlreadyExistsException;
import learning.griddynamics.com.storeapi.service.LoginAttemptService;
import learning.griddynamics.com.storeapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private LoginAttemptService loginAttemptService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(userService, loginAttemptService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void givenNewEmail_whenRegister_thenReturn200() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("john@example.com", "SecurePass123");
        doNothing().when(userService).register(request);

        // when / then
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).register(request);
    }

    @Test
    void givenExistingEmail_whenRegister_thenReturn409() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("john@example.com", "SecurePass123");
        doThrow(new UserAlreadyExistsException(request.email())).when(userService).register(request);

        // when / then
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnSessionId() throws Exception {
        // given
        LoginRequest request = new LoginRequest("john@example.com", "SecurePass123");
        User user = User.builder().id(UUID.randomUUID()).email("john@example.com").build();

        doNothing().when(loginAttemptService).checkAllowed(anyString());
        when(userService.authenticate("john@example.com", "SecurePass123")).thenReturn(user);
        doNothing().when(loginAttemptService).onLoginSucceeded(anyString());

        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").exists());
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenReturn401AndRegisterFailedAttempt() throws Exception {
        // given
        LoginRequest request = new LoginRequest("john@example.com", "wrongPassword");

        doNothing().when(loginAttemptService).checkAllowed(anyString());
        when(userService.authenticate("john@example.com", "wrongPassword"))
                .thenThrow(new InvalidCredentialsException());

        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService).onLoginFailed("john@example.com");
    }

    @Test
    void givenActiveSession_whenLogout_thenSessionIsInvalidated() throws Exception {
        // given / when
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }
}