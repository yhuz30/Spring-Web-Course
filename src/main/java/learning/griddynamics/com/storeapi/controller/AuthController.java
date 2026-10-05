package learning.griddynamics.com.storeapi.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import learning.griddynamics.com.storeapi.dto.request.LoginRequest;
import learning.griddynamics.com.storeapi.dto.request.RegisterRequest;
import learning.griddynamics.com.storeapi.dto.request.ResetPasswordRequest;
import learning.griddynamics.com.storeapi.dto.response.LoginResponse;
import learning.griddynamics.com.storeapi.entity.User;
import learning.griddynamics.com.storeapi.exception.InvalidCredentialsException;
import learning.griddynamics.com.storeapi.security.SessionAuthenticationFilter;
import learning.griddynamics.com.storeapi.service.LoginAttemptService;
import learning.griddynamics.com.storeapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public LoginResponse login(HttpSession session, @Valid @RequestBody LoginRequest request) {
        loginAttemptService.checkAllowed(request.email());

        try {
            User user = userService.authenticate(request.email(), request.password());
            loginAttemptService.onLoginSucceeded(request.email());

            session.setAttribute(SessionAuthenticationFilter.USER_ID_SESSION_ATTR, user.getId());
            return new LoginResponse(session.getId());
        } catch (InvalidCredentialsException ex) {
            loginAttemptService.onLoginFailed(request.email());
            throw ex;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate(); // removes the session from Redis, cart included
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.email());
    }
}