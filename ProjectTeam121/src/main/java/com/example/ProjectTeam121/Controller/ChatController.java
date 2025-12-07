package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Request.ChatRequest;
import com.example.ProjectTeam121.Dto.Response.ChatResponse;
import com.example.ProjectTeam121.Security.SQLValidator;
import com.example.ProjectTeam121.Service.AIService;
import com.example.ProjectTeam121.Service.ChatQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AIService aiService;

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> body) {

        String message = body.get("message");

        if (aiService.isVietnameseDateQuestion(message)) {
            return Map.of("answer", aiService.getVietnamDate());
        }

        if (aiService.isSQLQuestion(message)) {

            String sql = aiService.generateSQLOnly(message);

            if (sql == null) {
                return Map.of("answer", "AI không thể tạo câu SQL phù hợp.");
            }

            List<Map<String, Object>> result = aiService.runSQL(sql);

            String answer = aiService.explainResultToUser(message, result);

            return Map.of("answer", answer);
        }

        String answer = aiService.generalChat(message);

        return Map.of("answer", answer);
    }


}



