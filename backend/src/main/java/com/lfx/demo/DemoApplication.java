/**
 * 测盟汇系统 - Spring Boot 主启动类
 * 
 * 功能说明：
 * - 系统主入口，负责启动Spring Boot应用
 * - 配置MyBatis映射器扫描
 * - 启用方法级安全控制
 * - 自动执行数据库迁移和文件迁移
 * - 配置Tomcat服务器
 * 
 * @author Wang Shenjun
 * @date 2025.07.12
 * @version 1.0.0
 */
package com.lfx.demo;

import com.lfx.demo.util.DatabaseMigrationUtil;
import com.lfx.demo.util.FilePathConstants;
import com.lfx.demo.util.FileMigrationUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 测盟汇系统主应用类
 * 
 * 注解说明：
 * - @SpringBootApplication: Spring Boot自动配置
 * - @ComponentScan: 组件扫描范围
 * - @MapperScan: MyBatis映射器扫描
 * - @EnableMethodSecurity: 启用方法级安全控制
 */
@SpringBootApplication
@ComponentScan("com.lfx")
@MapperScan("com.lfx.demo.mapper")
@EnableMethodSecurity(prePostEnabled = true)
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private DatabaseMigrationUtil databaseMigrationUtil;

    /**
     * 配置Tomcat服务器工厂
     * 
     * 功能：自定义Tomcat配置，使用NIO协议提高性能
     * 
     * @return TomcatServletWebServerFactory 配置好的Tomcat工厂
     */
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setProtocol("org.apache.coyote.http11.Http11NioProtocol");
        return factory;
    }

    /**
     * 应用程序主入口方法
     * 
     * 启动流程：
     * 1. 创建必要的上传目录
     * 2. 启动Spring Boot应用
     * 3. 执行文件迁移（旧图片文件）
     * 4. 执行数据库迁移和结构检查
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 在应用启动时创建上传目录
        FilePathConstants.createUploadDirectories();
        
        // 启动Spring应用
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        
        // 执行文件迁移
        FileMigrationUtil migrationUtil = context.getBean(FileMigrationUtil.class);
        migrationUtil.migrateOldImageFiles();
        
        // 执行数据库迁移和同步检查
        try {
            DatabaseMigrationUtil dbMigrationUtil = context.getBean(DatabaseMigrationUtil.class);
            System.out.println("正在检查课程表结构...");
            dbMigrationUtil.ensurePublishCoursesTableStructure();
            System.out.println("正在检查会议表结构...");
            dbMigrationUtil.ensureConferenceTableStructure();
            System.out.println("正在生成数据同步报告...");
            dbMigrationUtil.printDataSyncReport();
            System.out.println("数据库初始化完成");
        } catch (Exception e) {
            System.err.println("数据库迁移失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CommandLineRunner接口实现方法
     * 
     * 功能：应用启动完成后自动执行数据库迁移
     * 注意：即使迁移失败也不会影响应用正常启动
     * 
     * @param args 命令行参数
     * @throws Exception 可能的异常
     */
    @Override
    public void run(String... args) throws Exception {
        // 应用启动时自动执行数据库迁移
        try {
            databaseMigrationUtil.runAllMigrations();
        } catch (Exception e) {
            System.err.println("数据库迁移执行失败: " + e.getMessage());
            // 不抛出异常，避免影响应用启动
        }
    }
}
