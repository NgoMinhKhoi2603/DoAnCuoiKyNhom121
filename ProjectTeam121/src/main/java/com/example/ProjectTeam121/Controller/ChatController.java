package com.example.ProjectTeam121.Controller;

import com.example.ProjectTeam121.Dto.Enum.QueryType;
import com.example.ProjectTeam121.Dto.Request.ChatRequest;
import com.example.ProjectTeam121.Dto.Response.ChatResponse;
import com.example.ProjectTeam121.Security.SQLValidator;
import com.example.ProjectTeam121.Service.AIService;
import com.example.ProjectTeam121.Service.ChatQueryService;
import com.example.ProjectTeam121.Service.QueryClassifier;
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
    private final QueryClassifier classifier;

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> body) {

        String message = body.get("message");
        QueryType type = classifier.classify(message);

        if (type == QueryType.SYSTEM_INFO) {
            return Map.of("answer", aiService.getVietnamDate());
        }

        if (type == QueryType.IOT_DATA) {
            String lakeAnswer = aiService.analyzeIoT(message);
            return Map.of("answer", lakeAnswer);
        }

        if (type == QueryType.DATA_QUERY) {

            String sql = aiService.generateSQLOnly(message);

            if (!aiService.isValidSQL(sql)) {
                return Map.of("answer", aiService.generalChat(message));
            }

            List<Map<String, Object>> result = aiService.runSQL(sql);
            String naturalAnswer = aiService.explainResultToUser(message, result);

            return Map.of("answer", naturalAnswer);
        }

        return Map.of("answer", aiService.generalChat(message));
    }

    public boolean isValidSQL(String sql) {
        if (sql == null) return false;
        String s = sql.trim().toLowerCase();
        return s.startsWith("select")   // không cho insert/update/delete
                && !s.contains(";")     // không cho chạy nhiều câu
                && !s.contains("--");   // chống SQL injection
    }


}



