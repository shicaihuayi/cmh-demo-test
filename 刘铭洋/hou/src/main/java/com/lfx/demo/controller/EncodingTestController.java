package com.lfx.demo.controller;

import com.lfx.demo.util.FileNameEncodingUtil;
import com.lfx.demo.web.domain.AjaxResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 编码测试控制器
 * 用于测试中文字符编码处理是否正常
 */
@RestController
@RequestMapping("/encoding-test")
public class EncodingTestController {

    /**
     * 测试中文字符处理
     */
    @GetMapping("/chinese")
    public AjaxResult testChinese(@RequestParam(defaultValue = "测试中文文件名.jpg") String filename) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("原始文件名", filename);
        result.put("包含中文", FileNameEncodingUtil.containsChinese(filename));
        result.put("URL编码后", FileNameEncodingUtil.safeUrlEncode(filename));
        result.put("URL解码后", FileNameEncodingUtil.safeUrlDecode(filename));
        result.put("清理后文件名", FileNameEncodingUtil.sanitizeFilename(filename));
        result.put("Content-Disposition", FileNameEncodingUtil.generateContentDisposition(filename));
        
        return AjaxResult.success(result);
    }
    
    /**
     * 测试各种特殊字符
     */
    @PostMapping("/special-chars")
    public AjaxResult testSpecialChars(@RequestBody Map<String, String> request) {
        String testString = request.getOrDefault("text", "测试文本：Hello 世界！@#$%^&*()");
        
        Map<String, Object> result = new HashMap<>();
        result.put("原始文本", testString);
        result.put("字符长度", testString.length());
        result.put("字节长度_UTF8", testString.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        result.put("包含中文", FileNameEncodingUtil.containsChinese(testString));
        result.put("URL编码", FileNameEncodingUtil.safeUrlEncode(testString));
        
        return AjaxResult.success(result);
    }
    
    /**
     * 返回服务器编码信息
     */
    @GetMapping("/server-info")
    public AjaxResult getServerInfo() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("默认字符集", java.nio.charset.Charset.defaultCharset().name());
        result.put("file.encoding", System.getProperty("file.encoding"));
        result.put("user.language", System.getProperty("user.language"));
        result.put("user.country", System.getProperty("user.country"));
        result.put("user.timezone", System.getProperty("user.timezone"));
        result.put("java.version", System.getProperty("java.version"));
        result.put("os.name", System.getProperty("os.name"));
        
        return AjaxResult.success(result);
    }
} 