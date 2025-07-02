package com.lfx.demo.controller;

import com.lfx.demo.util.FilePathConstants;
import com.lfx.demo.util.FileNameEncodingUtil;
import com.lfx.demo.web.domain.AjaxResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return AjaxResult.error("上传文件不能为空");
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return AjaxResult.error("文件名不能为空");
            }
            
            // 清理文件名中的特殊字符，确保文件系统兼容性
            String sanitizedFilename = FileNameEncodingUtil.sanitizeFilename(originalFilename);
            
            // 生成唯一的文件名，避免重名覆盖
            String uniqueFileName = UUID.randomUUID().toString() + "_" + sanitizedFilename;
            
            // 确保上传目录存在
            FilePathConstants.createUploadDirectories();
            
            // 构建文件保存的完整路径
            File destFile = new File(FilePathConstants.IMAGE_UPLOAD_PATH, uniqueFileName);
            
            // 保存文件
            file.transferTo(destFile);
            
            // 输出调试信息
            System.out.println("文件上传成功:");
            System.out.println("  原始文件名: " + originalFilename);
            System.out.println("  清理后文件名: " + sanitizedFilename);
            System.out.println("  唯一文件名: " + uniqueFileName);
            System.out.println("  保存路径: " + destFile.getAbsolutePath());
            System.out.println("  是否包含中文: " + FileNameEncodingUtil.containsChinese(originalFilename));

            // 生成访问URL - 对中文文件名进行URL编码
            String encodedFileName = FileNameEncodingUtil.safeUrlEncode(uniqueFileName);
            String fileUrl = "/public/" + encodedFileName;

            AjaxResult success = AjaxResult.success("文件上传成功");
            success.put("fileUrl", fileUrl);
            success.put("originalName", originalFilename);
            success.put("fileName", uniqueFileName);

            return success;
        } catch (IOException e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("文件上传异常: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error("文件上传异常: " + e.getMessage());
        }
    }
}
