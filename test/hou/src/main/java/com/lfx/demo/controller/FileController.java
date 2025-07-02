package com.lfx.demo.controller;
import com.lfx.demo.web.domain.AjaxResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    // private final String uploadPath = "public/upload";
    //public final String DEFAULT_UPLOAD_PATH = System.getProperty("user.dir") + "\\public\\";
    static final String DEFAULT_UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "public" + File.separator;



    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try {
            // 生成唯一的文件名，避免重名覆盖
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            // 构建文件保存的完整路径
            File parentDir = new File(DEFAULT_UPLOAD_PATH).getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            File destFile = new File(DEFAULT_UPLOAD_PATH, uniqueFileName);
            // 保存文件
            file.transferTo(destFile);

            String publicPath = File.separator + "public" + File.separator;
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/public/")
                    .path(uniqueFileName)
                    .toUriString();

            AjaxResult success = AjaxResult.success();
            success.put("fileUrl", fileUrl);

            return success;
        } catch (IOException e) {
            e.printStackTrace();
            return AjaxResult.error("文件上传失败");
        }
    }
}
