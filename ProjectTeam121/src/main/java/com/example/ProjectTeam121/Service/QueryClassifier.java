package com.example.ProjectTeam121.Service;

import com.example.ProjectTeam121.Dto.Enum.QueryType;
import org.springframework.stereotype.Component;
@Component
public class QueryClassifier {

    public QueryType classify(String message) {
        String msg = message.toLowerCase();

        if (msg.contains("iot") ||
                msg.contains("thiết bị") ||
                msg.contains("sensor") ||
                msg.contains("data lake") ||
                msg.contains("hồ dữ liệu") ||
                msg.contains("raw") ||
                msg.contains("minio")) {
            return QueryType.IOT_DATA;
        }

        if (msg.contains("bao nhiêu") ||
                msg.contains("liệt kê") ||
                msg.contains("danh sách") ||
                msg.contains("đếm") ||
                msg.matches(".*\\d{4}-\\d{2}-\\d{2}.*")) {
            return QueryType.DATA_QUERY;
        }

        return QueryType.SMALL_TALK;
    }
}

