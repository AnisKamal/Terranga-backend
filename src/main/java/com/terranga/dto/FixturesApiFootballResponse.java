package com.terranga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FixturesApiFootballResponse(List<FixtureData> response) {

    public record FixtureData(Fixture fixture, Teams teams, Status status) {}

    public record Status(@JsonProperty("long") String long_, @JsonProperty("short")String short_ ,String elapsed, String extra ){}

    public record Fixture(String date, Long id, String referee, Long timestamp) {}

    public record Teams(Team home, Team away) {}

    public record Team(String name, String logo) {}

}
