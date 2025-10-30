package com.example.ProjectTeam121.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncThreadConfig {

    @Bean(name = "virtualThreadCustom")
    public ExecutorService virtualThreadCustom() {
        return Executors.newFixedThreadPool(10);
    }



}
