package com.terranga.dto;

import java.util.List;

public record MatchesViewResponse(
        MatchsResponse next,
        List<MatchsResponse> lastMatches,
        List<MatchsResponse> upcomingMatches
) {}
