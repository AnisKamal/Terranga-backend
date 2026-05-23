package com.terranga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FixturesApiFootballResponse(List<FixtureData> response) {

    public record FixtureData(Fixture fixture, League league, Teams teams, Goals goals) {}

    public record Fixture(String date, Long id, String referee, Long timestamp, Status status) {}

    public record Status(@JsonProperty("long") String long_, @JsonProperty("short") String short_, Integer elapsed, String extra) {}

    public record League(Long id, String name, String country, String logo, String flag, Integer season, String round, String type) {}

    public record Teams(Team home, Team away) {}

    public record Team(Long id, String name, String logo) {}

    public record Goals(Integer home, Integer away) {}

}
