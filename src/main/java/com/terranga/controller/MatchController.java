package com.terranga.controller;

import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.service.FixturesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/matchs")
public class MatchController {

    private final FixturesService fixturesService;

    @GetMapping
    public List<MatchsResponse> getFixtureData() {
        log.info("=================== call get FixtureData ==================");
        return fixturesService.getAllMatches();
    }
}
