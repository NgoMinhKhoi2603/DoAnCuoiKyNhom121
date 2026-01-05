package com.example.ProjectTeam121.Service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    // Bucket mặc định cho việc upload ảnh/file thông thường từ Frontend
    @Value("${minio.bucket-name}")
    private String bucketName;

    // Bucket dành riêng cho Data Lake (AI Training - Parquet/JSON)
    @Value("${minio.datalake-bucket:ai-training-data}")
    private String dataLakeBucket;

    // ========================================================================
    // 1. Upload file (MultipartFile) từ Controller (Avatar, ảnh...)
    // ========================================================================
    public String uploadFile(MultipartFile file) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload: " + e.getMessage());
        }
    }

    // ========================================================================
    // 2. Download file (Tải file về máy)
    // ========================================================================
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

    // ========================================================================
    // 3. Xem file (Lấy Presigned URL)
    // ========================================================================
    public String getPresignedUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo link: " + e.getMessage());
        }
    }

    // ========================================================================
    // 4. API: Upload JSON String (Dùng nếu muốn lưu JSON thô)
    // ========================================================================
    public String uploadJson(String objectName, String jsonContent) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(dataLakeBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(dataLakeBucket).build());
            }

            byte[] contentBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
            InputStream stream = new ByteArrayInputStream(contentBytes);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(dataLakeBucket)
                            .object(objectName)
                            .stream(stream, contentBytes.length, -1)
                            .contentType("application/json")
                            .build()
            );

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload JSON lên Data Lake: " + e.getMessage());
        }
    }

    // ========================================================================
    // 5. Liệt kê file trong Data Lake
    // ========================================================================
    public List<String> listFiles(String prefix) {
        List<String> fileNames = new ArrayList<>();
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(dataLakeBucket).build());
            if (!found) {
                return fileNames;
            }

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(dataLakeBucket)
                            .prefix(prefix)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                fileNames.add(result.get().objectName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    // ========================================================================
    // 6. API MỚI: Upload Local File (Dùng cho Parquet Service)
    // ========================================================================
    public void uploadFile(String objectName, File file) {
        try {
            // Kiểm tra bucket Data Lake
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(dataLakeBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(dataLakeBucket).build());
            }

            // UploadObjectArgs dùng để upload file từ đường dẫn vật lý (Disk)
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(dataLakeBucket)
                            .object(objectName) // Tên file trên MinIO (VD: telemetry/.../file.parquet)
                            .filename(file.getAbsolutePath()) // Đường dẫn file tạm trên server
                            .contentType("application/octet-stream") // Dạng binary cho Parquet
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload file Parquet lên MinIO: " + e.getMessage());
        }
    }

    // ========================================================================
    // 7. Đọc nội dung file từ Data Lake
    // ========================================================================
    public String getFileContent(String fileName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(dataLakeBucket)
                        .object(fileName)
                        .build())) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file from Data Lake: " + fileName, e);
        }
    }
}