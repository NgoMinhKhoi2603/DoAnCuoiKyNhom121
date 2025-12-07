package com.example.ProjectTeam121.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatQueryService {


    private final JdbcTemplate jdbcTemplate;

    public Object executeSQL(String sql) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return "SQL ERROR: " + e.getMessage();
        }
    }

}

