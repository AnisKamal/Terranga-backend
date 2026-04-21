package com.terranga.mapper;

import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.entities.MatchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    MatchMapper INSTANCE = Mappers.getMapper(MatchMapper.class);

    @Mapping(source = "fixture.timestamp", target = "date")
    @Mapping(source = "teams.home.name", target = "homeName")
    @Mapping(source = "teams.home.logo", target = "homeLogo")
    @Mapping(source = "teams.away.name", target = "awayName")
    @Mapping(source = "teams.away.logo", target = "awayLogo")
    MatchsResponse mapFixtureToDtoMatch(FixturesApiFootballResponse.FixtureData fixtureData);

    List<MatchsResponse> mapFixtureListToMatchList(List<FixturesApiFootballResponse.FixtureData> fixtureDataList);

    @Mapping(source = "teams.home.name", target = "homeName")
    @Mapping(source = "teams.home.logo", target = "homeLogo")
    @Mapping(source = "teams.away.name", target = "awayName")
    @Mapping(source = "teams.away.logo", target = "awayLogo")
    @Mapping(source = "fixture.date", target = "date")
    @Mapping(source = "fixture.timestamp", target = "timestamp")
    @Mapping(source = "fixture.id", target = "idFixture")
    @Mapping(source = "fixture.referee", target = "referee")
    MatchEntity mapFixtureToMatchEntity(FixturesApiFootballResponse.FixtureData fixtureData);

    List<MatchEntity> mapFixtureListToMatchEntityList( List<FixturesApiFootballResponse.FixtureData> fixtureData);

}
