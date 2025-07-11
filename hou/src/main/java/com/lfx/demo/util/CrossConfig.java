package com.lfx.demo.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class CrossConfig implements WebMvcConfigurer {
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
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("配置静态资源路径...");
        System.out.println("图片上传路径: " + FilePathConstants.IMAGE_UPLOAD_PATH);
        System.out.println("视频上传路径: " + FilePathConstants.VIDEO_UPLOAD_PATH);
        
        // 核心修复：添加对 /prod-api/public/** 路径的支持
        registry.addResourceHandler("/prod-api/public/**")
                .addResourceLocations("file:" + FilePathConstants.IMAGE_UPLOAD_PATH + File.separator)
                .addResourceLocations("file:" + FilePathConstants.VIDEO_UPLOAD_PATH + File.separator);
        
        // 标准的 /public/** 路径映射
        registry.addResourceHandler("/public/**")
                .addResourceLocations("file:" + FilePathConstants.IMAGE_UPLOAD_PATH + File.separator)
                .addResourceLocations("file:" + FilePathConstants.VIDEO_UPLOAD_PATH + File.separator);
        
        // 标准的 /upload/** 路径映射
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + FilePathConstants.VIDEO_UPLOAD_PATH + File.separator)
                .addResourceLocations("file:" + FilePathConstants.IMAGE_UPLOAD_PATH + File.separator);
                
        // 确保上传目录存在
        FilePathConstants.createUploadDirectories();
        
        // 调试信息：打印实际的资源路径
        System.out.println("静态资源映射:");
        System.out.println("  /public/** -> file:" + FilePathConstants.IMAGE_UPLOAD_PATH + File.separator);
        System.out.println("  /upload/** -> file:" + FilePathConstants.VIDEO_UPLOAD_PATH + File.separator);
        System.out.println("  /prod-api/public/** -> file:" + FilePathConstants.IMAGE_UPLOAD_PATH + File.separator);
    }
}
