package com.csust.eco.service.impl;

import com.csust.eco.common.BizException;
import com.csust.eco.service.OssService;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    // 核心修复 1: 将初始化逻辑移至 Bean 创建后的生命周期钩子中
    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String policyJson = buildPublicReadPolicy(bucketName);
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policyJson).build());
                log.info("Bucket '{}' 初始化并配置公共读策略成功.", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO Bucket 初始化失败, 请检查存储服务!", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // 核心修复 2: 剔除每次上传的重复检查，只做纯粹的上传逻辑，极大地提升 I/O 吞吐量
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 推荐使用 try-with-resources 确保底层 InputStream 物理关闭，防止内存泄漏
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }

            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 构建 S3 标准的 Bucket 公共读策略 JSON 字符串
     */
    private String buildPublicReadPolicy(String bucketName) {
        return "{\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Action\": \"s3:GetObject\",\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": \"*\",\n" +
                "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"Version\": \"2012-10-17\"\n" +
                "}";
    }
}