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

    public boolean isSQLQuestion(String msg) {
        msg = msg.toLowerCase();

        boolean askData =
                msg.contains("bao nhiêu") ||
                        msg.contains("liệt kê") ||
                        msg.contains("danh sách") ||
                        msg.contains("đếm") ||
                        msg.contains("user") ||
                        msg.contains("thiết bị") ||
                        msg.contains("bản ghi");

        boolean hasDate =
                msg.contains("ngày") ||
                        msg.contains("tháng") ||
                        msg.contains("năm") ||
                        msg.matches(".*\\d{4}-\\d{2}-\\d{2}.*"); // yyyy-MM-dd

        return askData || hasDate;
    }


    public boolean isDateOnlyQuestion(String msg) {
        msg = msg.toLowerCase();
        return msg.contains("hôm nay") ||
                msg.contains("nay ngày") ||
                msg.contains("ngày bao nhiêu") ||
                msg.contains("thứ mấy");
    }

    public String answerToday() {
        LocalDate now = LocalDate.now();
        return "Hôm nay là " + now.toString();
    }

    public String generateSQLOnly(String question) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content",
                """
                Bạn là trợ lý AI của PTIT IoT Platform.

                Nhiệm vụ:
                - Chuyển câu hỏi thành SQL MySQL hợp lệ duy nhất.
                - Không giải thích, không trả về câu nào khác ngoài SQL.

                Quy tắc xử lý thời gian:
                - "hôm nay", "nay" → CURDATE()
                - "tháng này" → MONTH(created_at) = MONTH(CURDATE())
                - "năm nay" → YEAR(created_at) = YEAR(CURDATE())
                - "ngày 2024-12-01" → DATE(created_at) = '2024-12-01'

                Khi hỏi user → dùng bảng users.
                Khi hỏi thiết bị → dùng bảng devices.
                """
        ));

        messages.add(Map.of(
                "role", "user",
                "content",
                "Hãy viết DUY NHẤT câu SQL MySQL. Câu hỏi: " + question
        ));

        body.put("messages", messages);

        try {
            String response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            String sql = extractContent(response).trim();

            // fix lỗi model dùng bảng "user"
            sql = sql.replaceAll("\\buser\\b", "users");

            return sql;

        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> runSQL(String sql) {
        return jdbc.queryForList(sql);
    }

    public String explainResultToUser(String question, Object result) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        String content =
                """
                Hãy trả lời người dùng bằng tiếng Việt.
                Yêu cầu:
                - Không nhắc tới SQL hoặc database.
                - Trình bày rõ ràng, xuống dòng từng ý.
                - Ngắn gọn, dễ đọc, giống phong cách ChatGPT.

                Câu hỏi của người dùng:
                """ + question +
                        """
        
                        Đây là dữ liệu cần diễn giải:
                        """ + result;

        List<Map<String, String>> msgs = new ArrayList<>();
        msgs.add(Map.of("role", "user", "content", content));

        body.put("messages", msgs);

        try {
            String response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extractContent(response);

        } catch (Exception e) {
            return "Không thể diễn giải kết quả.";
        }
    }

    public String generalChat(String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, String>> msgs = new ArrayList<>();

        msgs.add(Map.of(
                "role", "system",
                "content",
                """
                Bạn là trợ lý AI của PTIT IoT Platform.
                Khi trả lời:
                - Trình bày rõ ràng, có xuống dòng.
                - Không dùng Markdown phức tạp.
                - Giải thích từng phần tách biệt, dễ đọc.
                """
        ));

        msgs.add(Map.of("role", "user", "content", message));

        body.put("messages", msgs);

        try {
            String response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extractContent(response);

        } catch (Exception e) {
            return "AI không phản hồi.";
        }
    }


    private String extractContent(String json) {
        try {
            var root = new ObjectMapper().readTree(json);
            return root.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            return "Không thể đọc phản hồi AI.";
        }
    }

    public boolean isVietnameseDateQuestion(String msg) {
        msg = msg.toLowerCase();

        return msg.contains("hôm nay") ||
                msg.contains("ngày mấy") ||
                msg.contains("ngày tháng năm") ||
                msg.contains("nay ngày") ||
                msg.contains("bây giờ là ngày") ||
                msg.contains("hnay") ||
                msg.contains("h.nay");
    }

    public String getVietnamDate() {
        LocalDate now = LocalDate.now();
        int day = now.getDayOfMonth();
        int month = now.getMonthValue();
        int year = now.getYear();

        return "Hôm nay là ngày " + day + " tháng " + month + " năm " + year + ".";
    }


}
