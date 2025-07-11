package com.lfx.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                    "/login", 
                    "/user/login", 
                    "/user/register", 
                    "/user/sendCode", 
                    "/user/send",
                    "/user/loadMyself",   // 允许检查登录状态
                    "/article/pageList",  // 允许公开访问文章列表
                    "/article/list",      // 允许公开访问文章列表
                    "/article/detail",    // 允许公开访问文章详情
                    "/confer/list",       // 允许公开访问会议列表
                    "/course/list",       // 允许公开访问课程列表
                    "/pagination-test/**", // 允许访问分页测试接口
                    "/pagination-verify/**", // 允许访问分页验证接口
                    "/encoding-test/**",  // 允许访问编码测试接口
                    "/public/**",         // 允许访问静态资源
                    "/upload/**",         // 允许访问上传的文件
                    "/prod-api/public/**", // 允许访问prod-api路径的静态资源
                    "/file/**",           // 允许访问文件上传接口
                    "/**/*.png",          // 允许访问图片文件
                    "/**/*.jpg",          // 允许访问图片文件
                    "/**/*.jpeg",         // 允许访问图片文件
                    "/**/*.gif",          // 允许访问图片文件
                    "/**/*.bmp"           // 允许访问图片文件
                ); // 排除登录、注册、发送验证码和公开查看类接口
    }

    /**
     * 配置静态资源处理，确保 API 请求不会被当作静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，确保只有特定路径的请求被当作静态资源
        registry.addResourceHandler("/public/**")
                .addResourceLocations("file:public/", "classpath:/public/");
        
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:upload/", "classpath:/upload/");
        
        registry.addResourceHandler("/prod-api/public/**")
                .addResourceLocations("file:public/", "classpath:/public/");
        
        // 确保 API 请求路径（如 /course/**）不被处理为静态资源
        // Spring Boot 会优先匹配控制器映射，然后才处理静态资源
    }
}