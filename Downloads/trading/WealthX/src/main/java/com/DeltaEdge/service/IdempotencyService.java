package com.DeltaEdge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "IDEMPOTENCY_";

    public boolean isDuplicate(String key) {
        // setIfAbsent is atomic: it returns true if the key was created, false if it already existed
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(PREFIX + key, "PROCESSING", 24, TimeUnit.HOURS);
        return Boolean.FALSE.equals(isNew);
    }

    public void saveKey(String key, String status) {
        redisTemplate.opsForValue().set(PREFIX + key, status, 24, TimeUnit.HOURS);
    }

    public void updateStatus(String key, String responseJson) {
        redisTemplate.opsForValue().set(PREFIX + key, responseJson, 24, TimeUnit.HOURS);
    }

    public String getPreviousResponse(String key) {
        return (String) redisTemplate.opsForValue().get(PREFIX + key);
    }

    // CRITICAL: Call this if the request fails (e.g., User not found)
    // so the user can try again with the same key after fixing the issue.
    public void removeKey(String key) {
        redisTemplate.delete(PREFIX + key);
    }
}