package com.DeltaEdge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    @Autowired(required = false) // Prevents Spring Boot from crashing on startup if Redis is missing
    private RedisTemplate<String, Object> redisTemplate;

    // SDE FIX: In-memory fallback cache if the Cloud Redis server is dead
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();

    private static final String PREFIX = "IDEMPOTENCY_";

    public boolean isDuplicate(String key) {
        String fullKey = PREFIX + key;
        try {
            if (redisTemplate != null) {
                // setIfAbsent is atomic: it returns true if the key was created, false if it already existed
                Boolean isNew = redisTemplate.opsForValue().setIfAbsent(fullKey, "PROCESSING", 24, TimeUnit.HOURS);
                return Boolean.FALSE.equals(isNew);
            }
        } catch (Exception e) {
            // Redis connection refused, silently failover to local memory
        }

        // ConcurrentHashMap putIfAbsent mimics Redis setIfAbsent atomically
        return localCache.putIfAbsent(fullKey, "PROCESSING") != null;
    }

    public void saveKey(String key, String status) {
        String fullKey = PREFIX + key;
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(fullKey, status, 24, TimeUnit.HOURS);
                return;
            }
        } catch (Exception e) {
            // Redis connection refused, failover
        }
        localCache.put(fullKey, status);
    }

    public void updateStatus(String key, String responseJson) {
        String fullKey = PREFIX + key;
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(fullKey, responseJson, 24, TimeUnit.HOURS);
                return;
            }
        } catch (Exception e) {
            // Redis connection refused, failover
        }
        localCache.put(fullKey, responseJson);
    }

    public String getPreviousResponse(String key) {
        String fullKey = PREFIX + key;
        try {
            if (redisTemplate != null) {
                return (String) redisTemplate.opsForValue().get(fullKey);
            }
        } catch (Exception e) {
            // Redis connection refused, failover
        }
        return localCache.get(fullKey);
    }

    // CRITICAL: Call this if the request fails (e.g., User not found)
    // so the user can try again with the same key after fixing the issue.
    public void removeKey(String key) {
        String fullKey = PREFIX + key;
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(fullKey);
                return;
            }
        } catch (Exception e) {
            // Redis connection refused, failover
        }
        localCache.remove(fullKey);
    }
}