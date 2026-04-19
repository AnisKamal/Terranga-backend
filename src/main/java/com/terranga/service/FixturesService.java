package com.terranga.service;

import com.terranga.client.ApiFootballClient;
import com.terranga.dto.FixturesApiFootballResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FixturesService {

    private final ApiFootballClient apiFootballClient;

    public FixturesApiFootballResponse getFixturesApiFootballResponse() {
        FixturesApiFootballResponse matchs = apiFootballClient.getMatch();
        return matchs;
    }
}
