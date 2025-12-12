package com.example.ProjectTeam121.Service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IoTLakeService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucket;

    /**
     * Đọc file mới nhất trong bucket Data Lake
     */
    public String loadLatestRawData() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .recursive(true)
                            .build()
            );

            Item latest = null;

            for (Result<Item> result : results) {
                Item item = result.get();
                if (latest == null || item.lastModified().isAfter(latest.lastModified())) {
                    latest = item;
                }
            }

            if (latest == null) {
                return null;
            }

            // Đọc nội dung file
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(latest.objectName())
                            .build()
            );

            return new String(stream.readAllBytes());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
