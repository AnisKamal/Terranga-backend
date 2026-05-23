package com.terranga.controller;

import com.terranga.dto.MatchesViewResponse;
import com.terranga.service.FixturesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/matchs")
public class MatchController {

    private final FixturesService fixturesService;

    @GetMapping
    public MatchesViewResponse getFixtureData() {
        log.info("=================== call get FixtureData (next + last matches) ==================");
        return fixturesService.getMatchesView();
    }
}
