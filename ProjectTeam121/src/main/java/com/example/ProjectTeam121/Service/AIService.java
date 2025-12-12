package com.example.ProjectTeam121.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${groq.model}")
    private String model;

    private final RestClient restClient;
    private final JdbcTemplate jdbc;
    private final IoTLakeService lake;

    /* =========================================================
                           1) DATE HANDLING
       ========================================================= */

    public boolean isVietnameseDateQuestion(String msg) {
        msg = msg.toLowerCase();
        return msg.contains("hôm nay")
                || msg.contains("ngày mấy")
                || msg.contains("ngày bao nhiêu")
                || msg.contains("ngày tháng năm");
    }

    public String getVietnamDate() {
        LocalDate now = LocalDate.now();
        return "Hôm nay là ngày " + now.getDayOfMonth()
                + " tháng " + now.getMonthValue()
                + " năm " + now.getYear() + ".";
    }

    /* =========================================================
                         2) SQL DETECTION + GENERATION
       ========================================================= */

    public boolean isSQLQuestion(String msg) {
        msg = msg.toLowerCase();
        return msg.contains("bao nhiêu")
                || msg.contains("đếm")
                || msg.contains("liệt kê")
                || msg.contains("danh sách")
                || msg.matches(".*\\d{4}-\\d{2}-\\d{2}.*");
    }

    public String generateSQLOnly(String question) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        body.put("messages", List.of(
                Map.of("role", "system",
                        "content", """
                        Bạn là AI sinh SQL MySQL.
                        Trả về DUY NHẤT 1 câu SQL hợp lệ.
                        Không giải thích.
                        Không mô tả.
                        Không bao gồm text khác.
                        """),
                Map.of("role", "user", "content", question)
        ));

        try {
            String resp = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extract(resp).trim();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isValidSQL(String sql) {
        if (sql == null) return false;

        String s = sql.trim().toLowerCase();

        return s.startsWith("select")        // chỉ SELECT
                && !s.contains(";")         // không cho nhiều câu
                && !s.contains("--")        // chống comment
                && !s.contains("drop")
                && !s.contains("delete")
                && !s.contains("update")
                && !s.contains("insert");
    }

    public List<Map<String, Object>> runSQL(String sql) {
        return jdbc.queryForList(sql);
    }

    /* =========================================================
                         3) IoT DATA LAKE ANALYSIS
       ========================================================= */

    public String analyzeIoT(String question) {

        String raw = lake.loadLatestRawData();

        if (raw == null) {
            return "Không tìm thấy dữ liệu nào trong Data Lake của bạn.";
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system",
                        "content", "Bạn là AI phân tích dữ liệu IoT."),
                Map.of("role", "user",
                        "content", "Dữ liệu cảm biến:\n" + raw + "\n\nCâu hỏi: " + question)
        ));

        try {
            String resp = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extract(resp);

        } catch (Exception e) {
            return "Không thể phân tích dữ liệu IoT.";
        }
    }

    /* =========================================================
                        4) GENERAL CHAT HANDLING
       ========================================================= */

    public String generalChat(String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system",
                        "content",
                        "Bạn là trợ lý AI. Trả lời rõ ràng, tự nhiên như ChatGPT."),
                Map.of("role", "user", "content", message)
        ));

        try {
            String resp = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extract(resp);

        } catch (Exception e) {
            return "AI không phản hồi.";
        }
    }

    /* =========================================================
                        5) NATURAL LANGUAGE RESPONSE
       ========================================================= */

    public String explainResultToUser(String question, Object result) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        String prompt = """
                Hãy trả lời người dùng bằng tiếng Việt.
                Không nhắc đến SQL hoặc cơ sở dữ liệu.
                Trình bày tự nhiên như ChatGPT.
                Dữ liệu truy vấn:
                """ + result + "\n\nCâu hỏi: " + question;

        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        try {
            String resp = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extract(resp);

        } catch (Exception e) {
            return "Không thể diễn giải kết quả.";
        }
    }

    /* =========================================================
                             6) JSON EXTRACTOR
       ========================================================= */

    private String extract(String json) {
        try {
            var root = new ObjectMapper().readTree(json);
            return root.get("choices").get(0)
                    .get("message").get("content").asText();
        } catch (Exception e) {
            return "Không đọc được phản hồi AI.";
        }
    }
}
