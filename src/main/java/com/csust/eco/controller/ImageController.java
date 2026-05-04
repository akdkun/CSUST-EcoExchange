package com.csust.eco.controller;

import com.csust.eco.common.Result;
import com.csust.eco.common.ResultCode;
import com.csust.eco.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片资源管理入口
 * 负责全局的商品图片、用户头像等视觉资产的上云调度
 */
@Tag(name = "4. 基础设施与存储模块", description = "提供全局图片上传与 OSS 调度服务")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    final private OssService ossService;

    @Operation(summary = "上传图片资源", description = "[需登录]支持全局图片上传, 自动返回对象存储的绝对路径 URL.限制大小 5MB.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(
            @Parameter(description = "要上传的图片文件 (最大5MB)", required = true)
            @RequestPart("file") MultipartFile file) {
        // 1. 简单的防御性校验
        if (file.isEmpty()) {
            return Result.failed(ResultCode.VALIDATE_FAILED, "上传图片不能为空");
        }

        // 2. 检查文件大小限制 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.failed(ResultCode.VALIDATE_FAILED, "图片大小不能超过 5MB");
        }

        // 3. (可选扩展) 校验文件后缀是否为图片格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase().matches(".*\\.(jpg|jpeg|png|webp|gif)$")) {
            return Result.failed(ResultCode.VALIDATE_FAILED, "仅支持 jpg, jpeg, png, webp, gif 格式的图片");
        }

        // 4. 调用底层的 OSS 服务
        String url = ossService.uploadFile(file);

        return Result.success(url);
    }
}