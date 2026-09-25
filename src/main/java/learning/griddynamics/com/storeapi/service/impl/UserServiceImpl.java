package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.dto.request.RegisterRequest;
import learning.griddynamics.com.storeapi.entity.User;
import learning.griddynamics.com.storeapi.exception.InvalidCredentialsException;
import learning.griddynamics.com.storeapi.exception.UserAlreadyExistsException;
import learning.griddynamics.com.storeapi.repository.UserRepository;
import learning.griddynamics.com.storeapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}