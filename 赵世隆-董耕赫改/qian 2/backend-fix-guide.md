# 后端静态资源配置修复指南

## 问题分析
前端请求的图片路径: `/prod-api/public/xxx.png`
但后端当前只配置了: `/upload/**` 映射到 `D:/video/upload/`

## 解决方案
需要在后端添加静态资源配置来支持 `/prod-api/public/**` 路径。

## 方法1: 修改现有的 InterceptorConfig 或 CrossConfig

在您的 Spring Boot 配置类中添加以下代码：

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 保留原有的配置
    registry.addResourceHandler("/upload/**")
            .addResourceLocations("file:D:/video/upload/");
    
    // 添加新的配置支持 prod-api 前缀
    registry.addResourceHandler("/prod-api/public/**")
            .addResourceLocations("file:public/");
            
    // 也添加不带前缀的访问
    registry.addResourceHandler("/public/**")
            .addResourceLocations("file:public/");
}
```

## 方法2: 创建新的配置类

创建一个新的配置类 `StaticResourceConfig.java`:

```java
package com.lfx.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 支持带 prod-api 前缀的公共资源访问
        registry.addResourceHandler("/prod-api/public/**")
                .addResourceLocations("file:public/");
        
        // 支持不带前缀的公共资源访问        
        registry.addResourceHandler("/public/**")
                .addResourceLocations("file:public/");
                
        // 兼容旧的upload路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:D:/video/upload/");
    }
}
```

## 测试方法
配置完成后，重启后端服务，然后测试这些URL能否正常访问：
- `http://localhost:8080/prod-api/public/xxx.png`
- `http://localhost:8080/public/xxx.png`
- `http://localhost:8080/upload/xxx.png`

## 注意事项
1. 确保 `public` 文件夹在项目根目录下
2. 确保文件夹有正确的读取权限
3. 重启后端服务使配置生效 