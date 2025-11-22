package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
public class MinioController {

    @Autowired
    private MinioService minioService;

    // API 1: Upload file
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file);
        return ResponseEntity.ok(fileName);
    }

    // API 2: Download file (Tải file về máy)
    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        InputStream stream = minioService.getFile(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    // API 3: Xem file (Trả về URL để gắn vào thẻ <img src="...">)
    @GetMapping("/view/{fileName}")
    public ResponseEntity<String> viewFile(@PathVariable String fileName) {
        String url = minioService.getPresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }
}