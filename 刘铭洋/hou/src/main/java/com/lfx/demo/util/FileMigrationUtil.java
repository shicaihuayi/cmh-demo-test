package com.lfx.demo.util;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 文件迁移工具类
 * 用于处理旧文件向新目录结构的迁移
 */
@Component
public class FileMigrationUtil {
    
    /**
     * 将D:/video/upload目录中的图片文件迁移到项目的public目录
     */
    public void migrateOldImageFiles() {
        File oldDir = new File(FilePathConstants.VIDEO_UPLOAD_PATH);
        File newDir = new File(FilePathConstants.IMAGE_UPLOAD_PATH);
        
        // 确保新目录存在
        if (!newDir.exists()) {
            newDir.mkdirs();
        }
        
        if (!oldDir.exists()) {
            System.out.println("旧目录不存在，无需迁移");
            return;
        }
        
        File[] files = oldDir.listFiles();
        if (files == null) {
            return;
        }
        
        int migratedCount = 0;
        for (File file : files) {
            if (file.isFile() && isImageFile(file.getName())) {
                try {
                    // 检查目标文件是否已存在
                    File targetFile = new File(newDir, file.getName());
                    if (!targetFile.exists()) {
                        Path sourcePath = file.toPath();
                        Path targetPath = targetFile.toPath();
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        migratedCount++;
                        System.out.println("迁移文件: " + file.getName());
                    }
                } catch (IOException e) {
                    System.err.println("迁移文件失败: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
        
        System.out.println("文件迁移完成，共迁移 " + migratedCount + " 个图片文件");
    }
    
    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String fileName) {
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".jpg") || 
               lowerCase.endsWith(".jpeg") || 
               lowerCase.endsWith(".png") || 
               lowerCase.endsWith(".gif") || 
               lowerCase.endsWith(".bmp");
    }
    
    /**
     * 检查文件是否存在于任意支持的目录中
     */
    public String findFileInSupportedDirectories(String fileName) {
        // 先在新目录中查找
        File newFile = new File(FilePathConstants.IMAGE_UPLOAD_PATH, fileName);
        if (newFile.exists()) {
            return "/public/" + fileName;
        }
        
        // 再在旧目录中查找
        File oldFile = new File(FilePathConstants.VIDEO_UPLOAD_PATH, fileName);
        if (oldFile.exists()) {
            return "/upload/" + fileName;
        }
        
        return null;
    }
} 