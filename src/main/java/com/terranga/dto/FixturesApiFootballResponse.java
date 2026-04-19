package com.terranga.dto;

import java.util.List;

public record FixturesApiFootballResponse(List<FixtureData> response) {

    public record FixtureData(Fixture fixture, Teams teams) {}

    public record Fixture(String date) {}

    public record Teams(Team home, Team away) {}

    public record Team(String name, String logo) {}

}
