package learning.griddynamics.com.storeapi.service.impl;

import learning.griddynamics.com.storeapi.exception.TooManyAttemptsException;
import learning.griddynamics.com.storeapi.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void checkAllowed(String email) {
        String attempts = redisTemplate.opsForValue().get(key(email));
        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            throw new TooManyAttemptsException();
        }
    }

    @Override
    public void onLoginFailed(String email) {
        String redisKey = key(email);
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, LOCKOUT_WINDOW);
        }
    }

    @Override
    public void onLoginSucceeded(String email) {
        redisTemplate.delete(key(email));
    }

    private String key(String email) {
        return "login:attempts:" + email;
    }
}