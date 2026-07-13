package com.kemp.dashboard.infrastructure.cache;

import com.kemp.dashboard.application.dto.DashboardResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private String getKey(UUID id) {
        return "dashboard:" + id.toString();
    }
    
    public DashboardResponse get(UUID id) {
        Object val = redisTemplate.opsForValue().get(getKey(id));
        if (val instanceof DashboardResponse) {
            return (DashboardResponse) val;
        }
        return null;
    }
    
    public void put(UUID id, DashboardResponse response) {
        redisTemplate.opsForValue().set(getKey(id), response, Duration.ofMinutes(5));
    }
    
    public void evict(UUID id) {
        redisTemplate.delete(getKey(id));
    }
}
