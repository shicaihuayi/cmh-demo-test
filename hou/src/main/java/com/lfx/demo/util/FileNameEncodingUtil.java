package com.lfx.demo.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件名编码工具类
 * 专门处理包含中文字符的文件名编码问题
 */
public class FileNameEncodingUtil {

    /**
     * 安全地对文件名进行URL解码
     * @param filename 需要解码的文件名
     * @return 解码后的文件名
     */
    public static String safeUrlDecode(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        
        try {
            // 尝试UTF-8解码
            String decoded = URLDecoder.decode(filename, StandardCharsets.UTF_8.name());
            return decoded;
        } catch (Exception e) {
            // 如果解码失败，返回原始文件名
            System.out.println("文件名解码失败，使用原始文件名: " + filename);
            return filename;
        }
    }

    /**
     * 安全地对文件名进行URL编码
     * @param filename 需要编码的文件名
     * @return 编码后的文件名
     */
    public static String safeUrlEncode(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        
        try {
            // 使用UTF-8编码，并将+号替换为%20
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
            return encoded;
        } catch (Exception e) {
            // 如果编码失败，返回原始文件名
            System.out.println("文件名编码失败，使用原始文件名: " + filename);
            return filename;
        }
    }

    /**
     * 生成符合RFC 5987标准的Content-Disposition头
     * @param filename 文件名
     * @return Content-Disposition头值
     */
    public static String generateContentDisposition(String filename) {
        String encodedFilename = safeUrlEncode(filename);
        return String.format("inline; filename*=UTF-8''%s", encodedFilename);
    }

    /**
     * 检查文件名是否包含中文字符
     * @param filename 文件名
     * @return 是否包含中文字符
     */
    public static boolean containsChinese(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        for (char c : filename.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清理文件名中的特殊字符，确保文件系统兼容性
     * @param filename 原始文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        
        // 移除或替换不安全的字符
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
} 