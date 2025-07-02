package com.lfx.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据库迁移工具类
 * 用于处理课程审核和会议审核相关的数据库结构优化
 */
@Component
public class DatabaseMigrationUtil {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 检查并创建 t_publishcourses 表的 course_id 字段
     */
    public void ensurePublishCoursesTableStructure() {
        try {
            // 检查 course_id 字段是否存在
            String checkColumnSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_publishcourses' AND COLUMN_NAME = 'course_id'";
            
            Integer columnExists = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);
            
            if (columnExists == null || columnExists == 0) {
                // 字段不存在，添加字段
                String addColumnSql = "ALTER TABLE t_publishcourses ADD COLUMN course_id INT";
                jdbcTemplate.execute(addColumnSql);
                System.out.println("已添加 t_publishcourses.course_id 字段");
                
                // 尝试根据名称匹配更新 course_id
                updateCourseIds();
            }
        } catch (Exception e) {
            System.err.println("数据库表结构检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查并创建 t_conference 表的 id 和 auditStatus 字段
     */
    public void ensureConferenceTableStructure() {
        try {
            // 先检查表是否存在主键
            String checkPrimaryKeySql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_conference' AND CONSTRAINT_NAME = 'PRIMARY'";
            
            Integer hasPrimaryKey = jdbcTemplate.queryForObject(checkPrimaryKeySql, Integer.class);
            
            // 如果没有主键，检查是否需要添加id字段
            if (hasPrimaryKey == null || hasPrimaryKey == 0) {
                // 检查 id 字段是否存在
                String checkIdColumnSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_conference' AND COLUMN_NAME = 'id'";
                
                Integer idColumnExists = jdbcTemplate.queryForObject(checkIdColumnSql, Integer.class);
                
                if (idColumnExists == null || idColumnExists == 0) {
                    // id字段不存在，添加自增主键字段
                    String addIdColumnSql = "ALTER TABLE t_conference ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY FIRST";
                    jdbcTemplate.execute(addIdColumnSql);
                    System.out.println("已添加 t_conference.id 字段作为主键");
                }
            } else {
                System.out.println("t_conference 表已有主键，跳过id字段添加");
            }
            
            // 检查 auditStatus 字段是否存在
            String checkAuditStatusColumnSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_conference' AND COLUMN_NAME = 'auditStatus'";
            
            Integer auditStatusColumnExists = jdbcTemplate.queryForObject(checkAuditStatusColumnSql, Integer.class);
            
            if (auditStatusColumnExists == null || auditStatusColumnExists == 0) {
                // auditStatus字段不存在，添加字段并设置默认值
                String addAuditStatusColumnSql = "ALTER TABLE t_conference ADD COLUMN auditStatus VARCHAR(20) DEFAULT '待审核'";
                jdbcTemplate.execute(addAuditStatusColumnSql);
                System.out.println("已添加 t_conference.auditStatus 字段");
                
                // 为现有记录设置默认审核状态
                String updateExistingRecordsSql = "UPDATE t_conference SET auditStatus = '待审核' WHERE auditStatus IS NULL";
                int updatedRows = jdbcTemplate.update(updateExistingRecordsSql);
                System.out.println("已为 " + updatedRows + " 条现有会议记录设置默认审核状态");
            } else {
                System.out.println("t_conference.auditStatus 字段已存在");
            }
        } catch (Exception e) {
            System.err.println("会议表结构检查失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据课程名称匹配更新 course_id
     */
    private void updateCourseIds() {
        try {
            String updateSql = "UPDATE t_publishcourses p " +
                    "INNER JOIN t_course c ON p.name = c.name " +
                    "SET p.course_id = c.id " +
                    "WHERE p.course_id IS NULL";
            
            int updatedRows = jdbcTemplate.update(updateSql);
            System.out.println("已更新 " + updatedRows + " 条发布课程的 course_id");
        } catch (Exception e) {
            System.err.println("更新 course_id 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 清理脏数据：删除在主表中不存在的发布课程记录
     */
    public void cleanOrphanedPublishCourses() {
        try {
            String deleteSql = "DELETE FROM t_publishcourses " +
                    "WHERE course_id IS NOT NULL " +
                    "AND course_id NOT IN (SELECT id FROM t_course)";
            
            int deletedRows = jdbcTemplate.update(deleteSql);
            System.out.println("已清理 " + deletedRows + " 条孤立的发布课程记录");
        } catch (Exception e) {
            System.err.println("清理孤立记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取数据同步状态报告
     */
    public void printDataSyncReport() {
        try {
            System.out.println("=== 课程数据同步状态报告 ===");
            
            // 主课程表统计
            String courseTotalSql = "SELECT COUNT(*) FROM t_course";
            Integer courseTotal = jdbcTemplate.queryForObject(courseTotalSql, Integer.class);
            System.out.println("主课程表总数: " + courseTotal);
            
            // 审核中的课程统计
            String auditingCourseSql = "SELECT COUNT(*) FROM t_course WHERE `pass` = '审核中'";
            Integer auditingCourses = jdbcTemplate.queryForObject(auditingCourseSql, Integer.class);
            System.out.println("审核中的课程: " + auditingCourses);
            
            // 发布表统计
            String publishTotalSql = "SELECT COUNT(*) FROM t_publishcourses";
            Integer publishTotal = jdbcTemplate.queryForObject(publishTotalSql, Integer.class);
            System.out.println("发布表记录总数: " + publishTotal);
            
            // 有效关联的发布记录
            String validPublishSql = "SELECT COUNT(*) FROM t_publishcourses p " +
                    "INNER JOIN t_course c ON p.course_id = c.id";
            Integer validPublish = jdbcTemplate.queryForObject(validPublishSql, Integer.class);
            System.out.println("有效关联的发布记录: " + validPublish);
            
            // 孤立的发布记录
            String orphanedPublishSql = "SELECT COUNT(*) FROM t_publishcourses " +
                    "WHERE course_id IS NULL OR course_id NOT IN (SELECT id FROM t_course)";
            Integer orphanedPublish = jdbcTemplate.queryForObject(orphanedPublishSql, Integer.class);
            System.out.println("孤立的发布记录: " + orphanedPublish);
            
            System.out.println("=== 会议数据状态报告 ===");
            
            // 会议表统计
            String conferTotalSql = "SELECT COUNT(*) FROM t_conference";
            Integer conferTotal = jdbcTemplate.queryForObject(conferTotalSql, Integer.class);
            System.out.println("会议表总数: " + conferTotal);
            
            // 各审核状态的会议统计
            String[] auditStatuses = {"待审核", "审核中", "通过", "不通过"};
            for (String status : auditStatuses) {
                String statusCountSql = "SELECT COUNT(*) FROM t_conference WHERE auditStatus = ?";
                Integer statusCount = jdbcTemplate.queryForObject(statusCountSql, Integer.class, status);
                System.out.println(status + "的会议: " + statusCount);
            }
            
            System.out.println("=== 报告结束 ===");
        } catch (Exception e) {
            System.err.println("生成数据同步报告失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 