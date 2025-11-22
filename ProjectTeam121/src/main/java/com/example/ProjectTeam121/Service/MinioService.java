package com.example.ProjectTeam121.Service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // 1. UPLOAD FILE
    public String uploadFile(MultipartFile file) {
        try {
            // Kiểm tra bucket, nếu chưa có thì tạo
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Tạo tên file duy nhất để tránh trùng lặp
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Upload
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType()) // Lưu content-type để trình duyệt hiểu file gì
                            .build()
            );

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload: " + e.getMessage());
        }
    }

    // 2. GET FILE DATA (Dùng cho tính năng Download về máy)
    public InputStream getFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lấy file: " + e.getMessage());
        }
    }

    // 3. GET PRESIGNED URL (Dùng để hiển thị ảnh/video trên Web/Mobile)
    // Link này sẽ có hiệu lực trong thời gian ngắn (ví dụ 1 giờ) bảo mật hơn public bucket
    public String getPresignedUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(1, TimeUnit.HOURS) // Link hết hạn sau 1 giờ
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo link: " + e.getMessage());
        }
    }
}