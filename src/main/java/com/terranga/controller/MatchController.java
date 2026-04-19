package com.terranga.controller;

import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.service.FixturesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/matchs")
public class MatchController {

    private final FixturesService fixturesService;

    @GetMapping
    public List<MatchsResponse> getFixtureData() {
        return fixturesService.getFixturesApiFootballResponse();
    }
}
