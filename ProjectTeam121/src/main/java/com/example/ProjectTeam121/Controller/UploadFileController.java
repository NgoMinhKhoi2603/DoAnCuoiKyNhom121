package com.example.ProjectTeam121.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Controller
@RequestMapping("/api/v1/uploadFile")
public class UploadFileController {

    // Xác định thư mục để lưu file upload
    private final Path fileStorageLocation;

    public UploadFileController() {
        // Lấy đường dẫn tới thư mục gốc của dự án và nối với "uploads"
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            // Tạo thư mục nếu nó chưa tồn tại
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục để lưu file upload.", ex);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể upload file trống.");
        }

        // Lấy tên file gốc
        String fileName = Objects.requireNonNull(file.getOriginalFilename());

        try {
            // Tạo đường dẫn đầy đủ tới file sẽ được lưu
            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            // Copy file vào thư mục đích (ghi đè nếu file đã tồn tại)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Tạo URL để truy cập file (tùy chọn)
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            return ResponseEntity.ok("Upload file thành công: " + fileName + ". Link download: " + fileDownloadUri);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể lưu file " + fileName + ". Vui lòng thử lại!");
        }
    }

}
