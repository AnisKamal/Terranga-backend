package com.terranga;

import com.terranga.service.FixturesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@RequiredArgsConstructor
@Slf4j
public class TerrangaApplication implements CommandLineRunner {

     public static void main(String[] args) {
        SpringApplication.run(TerrangaApplication.class, args);
    }

    private final  FixturesService fixturesService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Spring Boot Application");
        log.info("Information fixtures : \n  " + fixturesService.getFixturesApiFootballResponse().toString());
    }
}
