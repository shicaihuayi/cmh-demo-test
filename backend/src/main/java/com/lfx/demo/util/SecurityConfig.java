package com.lfx.demo.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/public/**", "/upload/**", "/prod-api/public/**").permitAll() // **允许公开访问静态资源**
                    .anyRequest().permitAll() // 暂时允许所有其他请求，以便开发
            )
            .csrf(csrf -> csrf.disable()) // 禁用CSRF保护，便于API测试
            .cors(withDefaults()) // 启用CORS，使用在别处定义的配置
            // 在标准的用户名密码认证过滤器之前，添加我们的自定义JWT过滤器
            .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 