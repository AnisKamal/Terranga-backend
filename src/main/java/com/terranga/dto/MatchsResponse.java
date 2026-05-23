package com.terranga.dto;

import java.util.List;

public record MatchsResponse(String date,
                             String homeName,
                             String homeLogo,
                             String awayName,
                             String awayLogo,
                             Integer homeGoals,
                             Integer awayGoals,
                             List<String> homeScorers,
                             List<String> awayScorers,
                             String competition)
{}
