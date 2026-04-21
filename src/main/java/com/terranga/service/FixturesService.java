package com.terranga.service;

import com.terranga.client.ApiFootballClient;
import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.entities.MatchEntity;
import com.terranga.mapper.MatchMapper;
import com.terranga.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FixturesService {

    private final ApiFootballClient apiFootballClient;

    private final MatchMapper mapper;

    private final MatchRepository matchRepository;

    @Value("${api.api-football.id.senegal-team}")
    private String idTeamSenegal;

    public List<MatchsResponse>  getFixturesApiFootballResponse() {
        FixturesApiFootballResponse matchs = apiFootballClient.getMatch(idTeamSenegal);
        //todo : calculer le current year pour l'ajouter à lapi
        List<MatchsResponse> matchsResponse = mapper.mapFixtureListToMatchList(matchs.response());


        return matchsResponse;
    }

    @Transactional
    public void  UpdateMatch(){
        FixturesApiFootballResponse matchs = apiFootballClient.getMatch(idTeamSenegal);

        List<MatchEntity> matchEntities = mapper.mapFixtureListToMatchEntityList(matchs.response());

        this.syncFixtures(matchEntities);
    }


    public void syncFixtures(List<MatchEntity> matches) {
        matches.forEach(m ->
                matchRepository.upsert(
                        m.getIdFixture(),
                        m.getTimestamp(),
                        m.getDate(),
                        m.getReferee(),
                        m.getHomeName(),
                        m.getHomeLogo(),
                        m.getAwayName(),
                        m.getAwayLogo()
                )
        );
    }
}
