package com.csust.eco.controller;

import com.csust.eco.common.Result;
import com.csust.eco.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片资源管理入口
 * 负责全局的商品图片、用户头像等视觉资产的上云调度
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        // 1. 简单的防御性校验
        if (file.isEmpty()) {
            return Result.error("上传图片不能为空");
        }

        // 2. 检查文件大小限制 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过 5MB");
        }

        // 3. (可选扩展) 校验文件后缀是否为图片格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.matches(".*\\.(jpg|jpeg|png|webp|gif)$")) {
            return Result.error("仅支持 jpg, jpeg, png, webp, gif 格式的图片");
        }

        // 4. 调用底层的 OSS 服务
        String url = ossService.uploadFile(file);

        return Result.success(url);
    }
}