package com.terranga.client;

import com.terranga.dto.FixtureEventsResponse;
import com.terranga.dto.FixturesApiFootballResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ApiFootballClient {

    private static final Logger log = LoggerFactory.getLogger(ApiFootballClient.class);

    private final  RestClient restClient;


    public ApiFootballClient( @Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public FixturesApiFootballResponse getMatch(String idTeam, int season){
        try {
            FixturesApiFootballResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/fixtures")
                            .queryParam("team", idTeam)
                            .queryParam("season", season)
                            .build())
                    .retrieve()
                    .body(FixturesApiFootballResponse.class);
            if (response == null || response.response() == null) {
                log.warn("Réponse API-Football vide (team={}, season={})", idTeam, season);
                return new FixturesApiFootballResponse(List.of());
            }
            return response;
        } catch (RestClientException e) {
            log.error("Échec de l'appel API-Football (team={}, season={}) : {}", idTeam, season, e.getMessage());
            return new FixturesApiFootballResponse(List.of());
        }
    }

    public FixtureEventsResponse getEvents(long fixtureId) {
        try {
            FixtureEventsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/fixtures/events")
                            .queryParam("fixture", fixtureId)
                            .build())
                    .retrieve()
                    .body(FixtureEventsResponse.class);
            if (response == null || response.response() == null) {
                log.warn("Réponse API-Football /events vide (fixture={})", fixtureId);
                return new FixtureEventsResponse(List.of());
            }
            return response;
        } catch (RestClientException e) {
            log.error("Échec de l'appel API-Football /events (fixture={}) : {}", fixtureId, e.getMessage());
            return new FixtureEventsResponse(List.of());
        }
    }

}
