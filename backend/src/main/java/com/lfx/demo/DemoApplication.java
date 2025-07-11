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

@SpringBootApplication
@ComponentScan("com.lfx")
@MapperScan("com.lfx.demo.mapper")
@EnableMethodSecurity(prePostEnabled = true)
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private DatabaseMigrationUtil databaseMigrationUtil;

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setProtocol("org.apache.coyote.http11.Http11NioProtocol");
        return factory;
    }

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
