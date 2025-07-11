package com.lfx.demo.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 一个简单的内存缓存，用于在开发环境中存储验证码。
 * 注意：这是一个非常基础的实现，不适用于生产环境。
 * 在生产环境中，应使用Redis或类似的技术。
 */
public class CodeStorage {

    private static final Map<String, String> codeMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 存储验证码，并设置5分钟后自动过期。
     * @param key 通常是手机号
     * @param value 验证码
     */
    public static void storeCode(String key, String value) {
        codeMap.put(key, value);
        // 设置一个5分钟后移除此键的任务
        scheduler.schedule(() -> codeMap.remove(key), 5, TimeUnit.MINUTES);
    }

    /**
     * 获取验证码。
     * @param key 手机号
     * @return 如果存在且未过期，则返回验证码，否则返回null。
     */
    public static String getCode(String key) {
        return codeMap.get(key);
    }
} 