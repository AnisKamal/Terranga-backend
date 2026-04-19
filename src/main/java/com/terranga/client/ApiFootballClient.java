package com.terranga.client;

import com.terranga.dto.FixturesApiFootballResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ApiFootballClient {

    private final  RestClient restClient;

    public ApiFootballClient( @Qualifier("apiFootballRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public FixturesApiFootballResponse getMatch(){
        return restClient.get()
                .uri("/fixtures?team=13&season=2022")
                .retrieve()
                .body(FixturesApiFootballResponse.class);
    }


}
