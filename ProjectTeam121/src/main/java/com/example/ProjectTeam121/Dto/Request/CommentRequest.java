package com.example.ProjectTeam121.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 2000, message = "Bình luận quá dài")
    private String content;

    // ID của bình luận cha (nếu là trả lời)
    private String parentId;
}