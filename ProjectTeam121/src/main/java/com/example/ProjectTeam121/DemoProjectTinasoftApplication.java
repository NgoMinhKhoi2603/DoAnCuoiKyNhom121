package com.example.ProjectTeam121;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EntityScan
@Slf4j
public class DemoProjectTinasoftApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoProjectTinasoftApplication.class, args);
	}

}
