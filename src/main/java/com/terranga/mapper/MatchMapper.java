package com.terranga.mapper;

import com.terranga.common.DateUtilities;
import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.entities.MatchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    MatchMapper INSTANCE = Mappers.getMapper(MatchMapper.class);

    @Mapping(source = "fixture.date", target = "date", qualifiedByName = "formatDate")
    @Mapping(source = "teams.home.name", target = "homeName")
    @Mapping(source = "teams.home.logo", target = "homeLogo")
    @Mapping(source = "teams.away.name", target = "awayName")
    @Mapping(source = "teams.away.logo", target = "awayLogo")
    @Mapping(source = "goals.home", target = "homeGoals")
    @Mapping(source = "goals.away", target = "awayGoals")
    @Mapping(source = "league.name", target = "competition", qualifiedByName = "normalizeCompetition")
    @Mapping(target = "homeScorers", ignore = true)
    @Mapping(target = "awayScorers", ignore = true)
    MatchsResponse mapFixtureToDtoMatch(FixturesApiFootballResponse.FixtureData fixtureData);

    List<MatchsResponse> mapFixtureListToMatchList(List<FixturesApiFootballResponse.FixtureData> fixtureDataList);

    @Mapping(source = "teams.home.id", target = "homeTeamId")
    @Mapping(source = "teams.home.name", target = "homeName")
    @Mapping(source = "teams.home.logo", target = "homeLogo")
    @Mapping(source = "teams.away.id", target = "awayTeamId")
    @Mapping(source = "teams.away.name", target = "awayName")
    @Mapping(source = "teams.away.logo", target = "awayLogo")
    @Mapping(source = "fixture.date", target = "date", qualifiedByName = "formatDate")
    @Mapping(source = "fixture.timestamp", target = "timestamp")
    @Mapping(source = "fixture.id", target = "idFixture")
    @Mapping(source = "fixture.referee", target = "referee")
    @Mapping(source = "fixture.status.short_", target = "statusShort")
    @Mapping(source = "goals.home", target = "homeGoals")
    @Mapping(source = "goals.away", target = "awayGoals")
    @Mapping(source = "league.name", target = "competition", qualifiedByName = "normalizeCompetition")
    @Mapping(target = "homeScorers", ignore = true)
    @Mapping(target = "awayScorers", ignore = true)
    MatchEntity mapFixtureToMatchEntity(FixturesApiFootballResponse.FixtureData fixtureData);

    List<MatchEntity> mapFixtureListToMatchEntityList(List<FixturesApiFootballResponse.FixtureData> fixtureData);

    @Mapping(source = "date", target = "date")
    @Mapping(source = "homeName", target = "homeName")
    @Mapping(source = "homeLogo", target = "homeLogo")
    @Mapping(source = "awayName", target = "awayName")
    @Mapping(source = "awayLogo", target = "awayLogo")
    @Mapping(source = "homeGoals", target = "homeGoals")
    @Mapping(source = "awayGoals", target = "awayGoals")
    @Mapping(source = "homeScorers", target = "homeScorers", qualifiedByName = "splitScorers")
    @Mapping(source = "awayScorers", target = "awayScorers", qualifiedByName = "splitScorers")
    @Mapping(source = "competition", target = "competition")
    MatchsResponse mapEntityToDtoMatch(MatchEntity matchEntity);

    List<MatchsResponse> mapEntitiesListToMatchList(List<MatchEntity> matchEntities);


    @Named("formatDate")
    default String formatDate(String date) {
        return DateUtilities.formaDateFromApiFootball(date);
    }

    @Named("splitScorers")
    default List<String> splitScorers(String scorers) {
        if (scorers == null || scorers.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scorers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Normalise le nom de compétition API-Football :
     * - "Friendlies" (ou variantes contenant "friend") → "Match amical"
     * - null / vide → "Match amical" (fallback défensif)
     * - sinon : nom original (ex: "Africa Cup of Nations", "World Cup - Qualification Africa")
     */
    @Named("normalizeCompetition")
    default String normalizeCompetition(String name) {
        if (name == null || name.isBlank()) return "Match amical";
        if (name.toLowerCase().contains("friend")) return "Match amical";
        return name;
    }
}
