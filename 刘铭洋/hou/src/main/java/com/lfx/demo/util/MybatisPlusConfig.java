package com.lfx.demo.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 启用分页插件实现真正的分页功能
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置MyBatis-Plus拦截器
     * 这是实现真正分页的关键配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件 - 这是启用真正分页功能的核心配置
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        
        // 设置最大单页限制数量，防止恶意请求
        paginationInterceptor.setMaxLimit(1000L);
        
        // 当超过最大页数时，不继续执行
        paginationInterceptor.setOverflow(false);
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        System.out.println("=== MyBatis-Plus 分页插件已配置 ===");
        System.out.println("数据库类型: MySQL");
        System.out.println("最大单页限制: 1000");
        System.out.println("分页插件已启用，现在可以实现真正的分页功能");
        System.out.println("SQL查询将自动添加 LIMIT 和 OFFSET 子句");
        
        return interceptor;
    }
} 