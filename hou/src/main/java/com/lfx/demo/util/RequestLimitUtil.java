package com.lfx.demo.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RequestLimitUtil {
    
    private final ConcurrentHashMap<String, AtomicLong> requestTimes = new ConcurrentHashMap<>();
    private final long minInterval = 300; // 最小间隔300毫秒，减少对正常用户的影响
    
    /**
     * 检查请求是否过于频繁
     * @param key 请求的唯一标识（比如：接口路径+用户ID）
     * @return true-允许请求，false-请求过于频繁
     */
    public boolean isAllowed(String key) {
        long currentTime = System.currentTimeMillis();
        AtomicLong lastTime = requestTimes.get(key);
        
        if (lastTime == null) {
            requestTimes.put(key, new AtomicLong(currentTime));
            return true;
        }
        
        if (currentTime - lastTime.get() < minInterval) {
            return false; // 请求过于频繁
        }
        
        lastTime.set(currentTime);
        return true;
    }
    
    /**
     * 清理过期的记录
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        requestTimes.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().get() > 60000); // 清理1分钟前的记录
    }
} 