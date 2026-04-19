package com.terranga.mapper;

import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    MatchMapper INSTANCE = Mappers.getMapper(MatchMapper.class);

    @Mapping(source = "fixture.date", target = "date")
    @Mapping(source = "teams.home.name", target = "homeName")
    @Mapping(source = "teams.home.logo", target = "homeLogo")
    @Mapping(source = "teams.away.name", target = "awayName")
    @Mapping(source = "teams.away.logo", target = "awayLogo")
    MatchsResponse mapFixtureToDtoMatch(FixturesApiFootballResponse.FixtureData fixtureData);

    List<MatchsResponse> mapFixtureToDtoMatchList(List<FixturesApiFootballResponse.FixtureData> fixtureDataList);
}
