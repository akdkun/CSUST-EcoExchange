package com.csust.eco.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    /**
     * 上传文件到 MinIO
     * @param file 前端传入的多部分文件对象
     * @return 文件的访问 URL
     */
    String uploadFile(MultipartFile file);
}