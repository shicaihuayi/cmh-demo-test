package com.lfx.demo.controller;

import com.lfx.demo.util.FilePathConstants;
import com.lfx.demo.web.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件测试控制器 - 用于诊断文件路径问题
 */
@RestController
@RequestMapping("/test")
public class FileTestController {

    @GetMapping("/paths")
    public AjaxResult testPaths() {
        Map<String, Object> result = new HashMap<>();
        
        // 当前工作目录
        String userDir = System.getProperty("user.dir");
        result.put("currentWorkingDirectory", userDir);
        
        // 配置的图片路径
        result.put("imageUploadPath", FilePathConstants.IMAGE_UPLOAD_PATH);
        result.put("videoUploadPath", FilePathConstants.VIDEO_UPLOAD_PATH);
        
        // 检查目录是否存在
        File imageDir = new File(FilePathConstants.IMAGE_UPLOAD_PATH);
        result.put("imageDirExists", imageDir.exists());
        result.put("imageDirAbsolutePath", imageDir.getAbsolutePath());
        
        // 列出public目录中的文件
        if (imageDir.exists()) {
            File[] files = imageDir.listFiles();
            result.put("filesInImageDir", files != null ? files.length : 0);
            if (files != null && files.length > 0) {
                String[] fileNames = new String[Math.min(files.length, 5)]; // 只显示前5个文件
                for (int i = 0; i < fileNames.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                result.put("sampleFileNames", fileNames);
            }
        }
        
        return AjaxResult.success(result);
    }
    
    @GetMapping("/file/{filename}")
    public AjaxResult checkFile(@PathVariable String filename) {
        Map<String, Object> result = new HashMap<>();
        
        File file = new File(FilePathConstants.IMAGE_UPLOAD_PATH, filename);
        result.put("fileName", filename);
        result.put("fullPath", file.getAbsolutePath());
        result.put("exists", file.exists());
        result.put("isFile", file.isFile());
        result.put("canRead", file.canRead());
        
        if (file.exists()) {
            result.put("size", file.length());
            result.put("lastModified", file.lastModified());
        }
        
        return AjaxResult.success(result);
    }
} 