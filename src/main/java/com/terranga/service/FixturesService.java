package com.terranga.service;

import com.terranga.client.ApiFootballClient;
import com.terranga.common.DateUtilities;
import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.entities.MatchEntity;
import com.terranga.mapper.MatchMapper;
import com.terranga.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FixturesService {

    private final ApiFootballClient apiFootballClient;
    private final MatchMapper mapper;
    private final MatchRepository matchRepository;
    private final CacheManager cacheManager;

    @Value("${api.api-football.id.senegal-team}")
    private String idTeamSenegal;

    @Cacheable(value = "matches", key = "'all'")
    public List<MatchsResponse> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(mapper::mapEntityToDtoMatch)
                .toList();
    }

    @Transactional
    public void  UpdateMatch(){
        //todo : calculer le current year pour l'ajouter à lapi
        FixturesApiFootballResponse matchs = apiFootballClient.getMatch(idTeamSenegal);
        List<MatchEntity> matchEntities = mapper.mapFixtureListToMatchEntityList(matchs.response());
        List<MatchsResponse> matchsResponseToCache = mapper.mapFixtureListToMatchList(matchs.response());
        this.saveIntoCache(matchsResponseToCache);
        this.syncFixtures(matchEntities);
    }

    private  void syncFixtures(List<MatchEntity> matches) {
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

    private void saveIntoCache(List<MatchsResponse> matchsResponse){
        Cache cache = cacheManager.getCache("matches");
        if (cache != null) {
            cache.clear();
            cache.put("all", matchsResponse);
        }
    }


}
