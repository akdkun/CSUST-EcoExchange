package com.csust.eco.service.impl;

import com.csust.eco.common.BizException;
import com.csust.eco.service.OssService;
import io.minio.*;
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

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // 1. 检查存储桶是否存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // 不存在则创建 Bucket
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully.", bucketName);

                // 核心补充: 自动应用公共读 (Public Read) 策略
                String policyJson = buildPublicReadPolicy(bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policyJson)
                                .build()
                );
                log.info("Bucket '{}' policy set to Public Read.", bucketName);
            }

            // 2. 构建唯一文件名 (按日期分片结构)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 3. 将文件流上传至 MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 4. 返回文件的绝对访问路径
            return endpoint + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            log.error("File upload failed", e);
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