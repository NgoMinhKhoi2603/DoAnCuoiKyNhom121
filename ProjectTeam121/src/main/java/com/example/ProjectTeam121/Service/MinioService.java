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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    // Bucket mặc định cho việc upload ảnh/file thông thường
    @Value("${minio.bucket-name}")
    private String bucketName;

    // Bucket dành riêng cho Data Lake (AI Training)
    // Bạn nhớ thêm dòng minio.datalake-bucket=ai-training-data vào application.properties
    // Hoặc nếu muốn dùng chung bucket, bạn có thể sửa code bên dưới để dùng biến this.bucketName
    @Value("${minio.datalake-bucket:ai-training-data}")
    private String dataLakeBucket;

    // ========================================================================
    // 1. Upload file (MultipartFile) từ Controller
    // ========================================================================
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
                            .expiry(1, TimeUnit.HOURS) // Link hết hạn sau 1 giờ
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo link: " + e.getMessage());
        }
    }

    // ========================================================================
    // 4. API MỚI: Upload JSON String (Dùng cho AI Data Lake)
    // ========================================================================
    public String uploadJson(String objectName, String jsonContent) {
        try {
            // Kiểm tra bucket Data Lake, nếu chưa có thì tạo
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(dataLakeBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(dataLakeBucket).build());
            }

            // Chuyển chuỗi JSON thành luồng bytes
            byte[] contentBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
            InputStream stream = new ByteArrayInputStream(contentBytes);

            // Upload với content-type là application/json
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

    // 4. Liệt kê file trong Data Lake (Sửa để dùng dataLakeBucket)
    public List<String> listFiles(String prefix) {
        List<String> fileNames = new ArrayList<>();
        try {
            // Kiểm tra bucket tồn tại chưa để tránh lỗi
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(dataLakeBucket).build());
            if (!found) {
                return fileNames; // Trả về list rỗng nếu chưa có bucket
            }

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(dataLakeBucket) // QUAN TRỌNG: Dùng bucket Data Lake
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

    // 5. Đọc nội dung file từ Data Lake (Sửa để dùng dataLakeBucket)
    public String getFileContent(String fileName) {
        // QUAN TRỌNG: Không dùng hàm getFile() cũ vì nó trỏ vào test-bucket
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(dataLakeBucket) // QUAN TRỌNG: Dùng bucket Data Lake
                        .object(fileName)
                        .build())) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file from Data Lake: " + fileName, e);
        }
    }
}