package com.lfx.demo.controller;

import com.lfx.demo.util.FilePathConstants;
import com.lfx.demo.util.FileNameEncodingUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 静态资源控制器
 * 处理图片和文件的访问，提供更好的兼容性和中文字符支持
 */
@RestController
public class StaticResourceController {

    /**
     * 处理 /public/ 路径下的文件访问
     */
    @GetMapping("/public/{filename:.+}")
    public ResponseEntity<Resource> getPublicFile(@PathVariable String filename) {
        return getFileResponse(filename, FilePathConstants.IMAGE_UPLOAD_PATH, FilePathConstants.VIDEO_UPLOAD_PATH);
    }

    /**
     * 处理 /upload/ 路径下的文件访问
     */
    @GetMapping("/upload/{filename:.+}")
    public ResponseEntity<Resource> getUploadFile(@PathVariable String filename) {
        return getFileResponse(filename, FilePathConstants.VIDEO_UPLOAD_PATH, FilePathConstants.IMAGE_UPLOAD_PATH);
    }

    /**
     * 处理 /prod-api/public/ 路径下的文件访问（兼容前端路径）
     */
    @GetMapping("/prod-api/public/{filename:.+}")
    public ResponseEntity<Resource> getProdApiFile(@PathVariable String filename) {
        return getFileResponse(filename, FilePathConstants.IMAGE_UPLOAD_PATH, FilePathConstants.VIDEO_UPLOAD_PATH);
    }

    /**
     * 通用文件响应方法 - 增强中文字符支持
     */
    private ResponseEntity<Resource> getFileResponse(String filename, String primaryPath, String fallbackPath) {
        try {
            // 使用工具类安全解码文件名
            String decodedFilename = FileNameEncodingUtil.safeUrlDecode(filename);
            
            // 先在主目录中查找
            File file = new File(primaryPath, decodedFilename);
            if (!file.exists()) {
                // 如果主目录中没有，在备用目录中查找
                file = new File(fallbackPath, decodedFilename);
            }

            if (file.exists() && file.isFile()) {
                Resource resource = new FileSystemResource(file);
                
                // 获取文件类型
                Path path = file.toPath();
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                // 构建响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                
                // 使用工具类生成Content-Disposition头
                String disposition = FileNameEncodingUtil.generateContentDisposition(decodedFilename);
                headers.set(HttpHeaders.CONTENT_DISPOSITION, disposition);
                
                // 添加其他必要的头信息
                headers.set("Content-Transfer-Encoding", "binary");
                headers.set("Cache-Control", "max-age=3600");
                headers.set("Accept-Ranges", "bytes");
                
                // 如果包含中文字符，添加额外的编码信息
                if (FileNameEncodingUtil.containsChinese(decodedFilename)) {
                    headers.set("Content-Language", "zh-CN");
                }
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                System.out.println("文件未找到: " + decodedFilename + " 在路径: " + primaryPath + " 和 " + fallbackPath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("文件访问错误: " + e.getMessage());
            System.err.println("请求的文件名: " + filename);
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 