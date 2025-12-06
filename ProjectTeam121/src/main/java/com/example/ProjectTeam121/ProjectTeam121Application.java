package com.example.ProjectTeam121;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EntityScan
@Slf4j
public class ProjectTeam121Application {

	public static void main(String[] args) {
		SpringApplication.run(ProjectTeam121Application.class, args);
	}

}
