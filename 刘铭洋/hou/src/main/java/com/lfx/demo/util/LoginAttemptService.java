package com.lfx.demo.util;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginAttemptService {
    
    private final int MAX_ATTEMPT = 5; // 最大尝试次数
    private final ConcurrentHashMap<String, AtomicInteger> attemptCache = new ConcurrentHashMap<>();
    
    public void loginSucceeded(String key) {
        attemptCache.remove(key);
    }
    
    public void loginFailed(String key) {
        AtomicInteger attempts = attemptCache.computeIfAbsent(key, k -> new AtomicInteger(0));
        attempts.incrementAndGet();
    }
    
    public boolean isBlocked(String key) {
        AtomicInteger attempts = attemptCache.get(key);
        return attempts != null && attempts.get() >= MAX_ATTEMPT;
    }
    
    public void resetAttempts(String key) {
        attemptCache.remove(key);
    }
    
    public int getAttemptCount(String key) {
        AtomicInteger attempts = attemptCache.get(key);
        return attempts == null ? 0 : attempts.get();
    }
} 