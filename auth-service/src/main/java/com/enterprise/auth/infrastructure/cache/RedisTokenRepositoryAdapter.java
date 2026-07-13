package com.enterprise.auth.infrastructure.cache;

import com.enterprise.auth.domain.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenRepositoryAdapter implements TokenRepository {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "session:refresh:";

    @Override
    public void saveRefreshToken(String userId, String refreshToken, long durationMs) {
        String key = KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, durationMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean existsByUserIdAndRefreshToken(String userId, String refreshToken) {
        String key = KEY_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }

    @Override
    public void revokeRefreshToken(String userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    private static final String RESET_PREFIX = "pwd:reset:";

    @Override
    public void savePasswordResetToken(String token, String email, long durationMs) {
        redisTemplate.opsForValue().set(RESET_PREFIX + token, email, durationMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getEmailByPasswordResetToken(String token) {
        return redisTemplate.opsForValue().get(RESET_PREFIX + token);
    }

    @Override
    public void deletePasswordResetToken(String token) {
        redisTemplate.delete(RESET_PREFIX + token);
    }
}
