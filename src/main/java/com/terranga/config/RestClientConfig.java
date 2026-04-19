package com.terranga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Value("${api.api-football.base-url}")
    private String apiFootballBaseUrl ;

    @Value("${api.api-football.header.key}")
    private String apiFootBallHeaderKey;

    @Value("${api.api-football.header.value}")
    private String apiFootBallHeaderValue;

    @Bean
    public RestClient apiFootballRestClient() {
        return  RestClient.builder()
                .baseUrl(apiFootballBaseUrl)
                .defaultHeader(apiFootBallHeaderKey,apiFootBallHeaderValue)
                .build();
    }

}
