package com.lfx.demo.util;

import java.io.File;

/**
 * 文件路径常量类
 * 统一管理文件上传路径
 */
public class FilePathConstants {
    
    /**
     * 图片上传路径 - 用于文章中的图片
     */
    public static final String IMAGE_UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "public";
    
    /**
     * 视频上传路径 - 用于视频文件
     */
    public static final String VIDEO_UPLOAD_PATH = "D:" + File.separator + "video" + File.separator + "upload";
    
    /**
     * 创建必要的上传目录
     */
    public static void createUploadDirectories() {
        File imageDir = new File(IMAGE_UPLOAD_PATH);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        
        File videoDir = new File(VIDEO_UPLOAD_PATH);
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }
    }
} 