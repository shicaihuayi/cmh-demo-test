package com.lfx.demo.util;

import com.lfx.demo.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    // 1. 定义一个固定的密钥字符串，以确保服务器重启后密钥不变。
    // 在生产环境中，强烈建议将此密钥存储在配置文件或环境变量中，而不是硬编码。
    private static final String SECRET_STRING = "cemenghui-project-secret-key-that-is-long-and-secure-enough-for-hs256-a1b2c3d4e5f6";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // 2. Token的过期时间（例如，30天）
    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 30; // 30 days

    /**
     * 生成JWT Token
     * @param user 用户信息
     * @return Token字符串
     */
    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getName());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 提供一个公共方法来获取密钥，供过滤器使用
     * @return SecretKey
     */
    public static SecretKey getSecretKey() {
        return SECRET_KEY;
    }
} 