package com.lfx.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import java.io.File;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("POST","GET","PUT","OPTIONS","DELETE")
                .maxAge(168000)
                .allowedHeaders("*")
                .allowCredentials(true);
    }

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
                    "/app/user/login",    // 小程序手机号登录
                    "/app/user/wxLogin",  // 小程序微信登录
                    "/app/user/register", // 小程序用户注册
                    "/app/user/send",     // 小程序发送验证码
                    "/app/conference/**", // 小程序会议接口
                    "/app/article/**",    // 小程序动态接口
                    "/app/course/list",   // 小程序课程列表（公开）
                    "/app/home/**",      // 小程序首页接口
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
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 新增：为/public/** 添加一个通用的资源处理器，作为对旧错误URL的兼容
        // 它会捕获所有/public/下的请求，并尝试在IMAGE_UPLOAD_PATH中寻找文件
        registry.addResourceHandler("/public/**")
                .addResourceLocations("file:" + FilePathConstants.IMAGE_UPLOAD_PATH + File.separator);

        // 原有的配置：为图片和视频配置更具体的静态资源处理器
        // Spring会优先匹配更具体的模式，所以/public/images/下的请求仍由这条规则处理
        registry.addResourceHandler("/public/images/**")
                .addResourceLocations("file:" + FilePathConstants.IMAGE_UPLOAD_PATH + File.separator);
        
        // 映射/public/videos/**到硬盘上的视频目录
        registry.addResourceHandler("/public/videos/**")
                .addResourceLocations("file:" + FilePathConstants.VIDEO_UPLOAD_PATH + File.separator);

        // 确保上传目录存在
        FilePathConstants.createUploadDirectories();
    }
}