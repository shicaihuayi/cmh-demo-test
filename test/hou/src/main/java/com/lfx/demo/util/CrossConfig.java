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

    static final String DEFAULT_UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "public" + File.separator;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println(DEFAULT_UPLOAD_PATH);
        /** 本地文件上传路径 */
        registry.addResourceHandler(
                        "/public" + "/**")
                .addResourceLocations("file:" + DEFAULT_UPLOAD_PATH);
    }
}
