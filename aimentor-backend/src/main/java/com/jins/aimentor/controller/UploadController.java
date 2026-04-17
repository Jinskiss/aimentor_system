package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 * 处理头像上传等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@Api(tags = "文件上传", description = "文件上传接口")
public class UploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @ApiOperation("上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 允许未登录用户上传（注册时需要），登录用户会记录userId
        Long userId = UserHolder.getUser() != null ? UserHolder.getUser().getId() : null;

        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过5MB");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名无效");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.error("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        try {
            // 使用绝对路径，确保目录在项目根目录
            String projectDir = System.getProperty("user.dir");
            String dateDir = LocalDate.now().toString().replace("-", "");
            String relativePath = "avatar" + File.separator + dateDir;
            String uploadDir = projectDir + File.separator + uploadPath + File.separator + relativePath;

            // 创建上传目录（确保父目录也存在）
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("[Upload] 创建上传目录: {}, 结果: {}", uploadDir, created);
            }

            // 生成新文件名
            String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            String fullPath = uploadDir + File.separator + newFileName;

            // 保存文件到目标位置
            File destFile = new File(fullPath);
            file.transferTo(destFile);

            log.info("[Upload] 头像上传成功，userId={}, path={}", userId, fullPath);

            // 返回访问URL
            String avatarUrl = baseUrl + "/uploads/" + relativePath.replace("\\", "/") + "/" + newFileName;
            return Result.success(avatarUrl);
        } catch (IOException e) {
            log.error("[Upload] 头像上传失败: {}", e.getMessage(), e);
            return Result.error("上传失败，请重试");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}