package com.terranga.service;

import com.terranga.client.ApiFootballClient;
import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.mapper.MatchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FixturesService {

    private final ApiFootballClient apiFootballClient;

    private final MatchMapper mapper;

    @Value("${api.api-football.id.senegal-team}")
    private String idTeamSenegal;

    public List<MatchsResponse>  getFixturesApiFootballResponse() {
        FixturesApiFootballResponse matchs = apiFootballClient.getMatch(idTeamSenegal);
        //todo : calculer le current year pour l'ajouter à lapi
        List<MatchsResponse> matchsResponse = mapper.mapFixtureToDtoMatchList(matchs.response());

        return matchsResponse;
    }
}
