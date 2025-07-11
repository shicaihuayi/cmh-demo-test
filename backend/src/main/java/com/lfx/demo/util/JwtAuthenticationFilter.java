package com.lfx.demo.util;

import com.lfx.demo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.debug("JWT Filter处理请求: {}", requestURI);

        // 1. 从请求头中获取Token
        String token = request.getHeader("Authorization");

        // 2. 检查Token是否存在且格式正确
        if (token != null && token.startsWith("Bearer ")) {
            try {
                // 去掉 "Bearer " 前缀
                token = token.substring(7);

                // 3. 解析Token
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(JwtUtil.getSecretKey()) // 使用JwtUtil中的密钥
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // 4. 从Token中提取用户信息
                Integer userId = claims.get("id", Integer.class);
                String name = claims.get("name", String.class);
                String role = claims.get("role", String.class);

                if (userId != null && name != null && role != null) {
                    User user = new User();
                    user.setId(userId);
                    user.setName(name);
                    user.setRole(role);

                    // 升级1：将用户信息放入 SecurityContext，供 AuthUtils 使用
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, // principal, 可以是User对象
                            null, // credentials, 密码（不需要）
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) // 权限
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 升级2：同时将用户信息放入 Session，供 LoginInterceptor 等旧代码使用
                    HttpSession session = request.getSession();
                    if (session.getAttribute("login_user") == null) {
                        session.setAttribute("login_user", user);
                    }
                    
                    logger.info("JWT认证成功，已将用户 '{}' 信息同时注入SecurityContext和Session。", name);
                } else {
                    logger.warn("JWT token解析成功，但缺少必要的用户信息字段 (id={}, name={}, role={})", userId, name, role);
                }
            } catch (ExpiredJwtException e) {
                logger.warn("JWT已过期: {}", e.getMessage());
                // Token过期，清理认证信息
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
            } catch (JwtException e) {
                logger.warn("JWT验证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                // 其他错误
                logger.warn("JWT处理异常: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            // 对于没有提供Token的请求，记录但继续处理，后续的Filter或Interceptor会决定是否拦截
            logger.debug("请求没有提供有效的JWT Token: {}", requestURI);
        }

        // 7. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
} 