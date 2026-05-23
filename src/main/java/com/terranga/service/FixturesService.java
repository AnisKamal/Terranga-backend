package com.terranga.service;

import com.terranga.client.ApiFootballClient;
import com.terranga.common.DateUtilities;
import com.terranga.dto.FixtureEventsResponse;
import com.terranga.dto.FixturesApiFootballResponse;
import com.terranga.dto.MatchesViewResponse;
import com.terranga.dto.MatchsResponse;
import com.terranga.entities.MatchEntity;
import com.terranga.mapper.MatchMapper;
import com.terranga.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FixturesService {

    private final ApiFootballClient apiFootballClient;
    private final MatchMapper mapper;
    private final MatchRepository matchRepository;
    private final CacheManager cacheManager;

    @Value("${api.api-football.id.senegal-team}")
    private String idTeamSenegal;

    @Value("${api.api-football.season:}")
    private String configuredSeason;

    @Cacheable(value = "matches", key = "'all'")
    public List<MatchsResponse> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(mapper::mapEntityToDtoMatch)
                .toList();
    }

    public Optional<MatchsResponse> getNextMatch() {
        long nowTimestamp = System.currentTimeMillis() / 1000;
        return matchRepository.findNextMatch(nowTimestamp)
                .map(mapper::mapEntityToDtoMatch);
    }

    public MatchesViewResponse getMatchesView() {
        long now = System.currentTimeMillis() / 1000;
        MatchsResponse next = matchRepository.findNextMatch(now)
                .map(mapper::mapEntityToDtoMatch)
                .orElse(null);
        List<MatchsResponse> lastMatches = matchRepository.findLastFinishedMatches(2).stream()
                .map(mapper::mapEntityToDtoMatch)
                .toList();
        List<MatchsResponse> upcomingMatches = matchRepository.findUpcomingMatches(now).stream()
                .map(mapper::mapEntityToDtoMatch)
                .toList();
        return new MatchesViewResponse(next, lastMatches, upcomingMatches);
    }

    @Transactional
    public void UpdateMatch() {
        // Vider le cache EN PREMIER : élimine les données stales (ancien format de sérialisation, etc.)
        evictCache();

        int season = resolveSeason();
        int currentYear = LocalDate.now().getYear();

        // Fetch saison courante (ex : 2025 pour les ligues de clubs)
        List<FixturesApiFootballResponse.FixtureData> allFixtures = new ArrayList<>(fetchFixtures(season));

        // Si la saison calculée ≠ année calendaire (ex : mai 2026 → season=2025, year=2026),
        // on fetch aussi l'année en cours pour couvrir la Coupe du Monde 2026 et les matchs amicaux.
        if (currentYear != season) {
            allFixtures.addAll(fetchFixtures(currentYear));
        }

        if (allFixtures.isEmpty()) {
            return;
        }

        // Déduplication par fixture.id (une même rencontre ne doit apparaître qu'une fois)
        List<FixturesApiFootballResponse.FixtureData> deduplicated = allFixtures.stream()
                .filter(f -> f.fixture() != null && f.fixture().id() != null)
                .collect(Collectors.toMap(
                        f -> f.fixture().id(),
                        f -> f,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();

        List<MatchEntity> matchEntities = mapper.mapFixtureListToMatchEntityList(deduplicated);
        List<MatchsResponse> matchsResponseToCache = mapper.mapFixtureListToMatchList(deduplicated);
        this.saveIntoCache(matchsResponseToCache);
        this.syncFixtures(matchEntities);

        // Enrichir les 2 derniers matchs terminés avec les noms des buteurs (1 appel API par match).
        this.enrichScorersForLastFinishedMatches(2);
    }

    /**
     * Pour les N derniers matchs FT qui n'ont pas encore de buteurs en DB, appelle /fixtures/events
     * pour récupérer la liste des buts puis stocke les noms (séparés par ", ") par équipe.
     */
    private void enrichScorersForLastFinishedMatches(int limit) {
        List<MatchEntity> toEnrich = matchRepository.findLastFinishedMatchesWithoutScorers(limit);
        for (MatchEntity match : toEnrich) {
            FixtureEventsResponse events = apiFootballClient.getEvents(match.getIdFixture());
            if (events.response() == null) continue;

            String homeNames = events.response().stream()
                    .filter(e -> "Goal".equalsIgnoreCase(e.type()))
                    .filter(e -> e.team() != null && match.getHomeTeamId() != null
                            && match.getHomeTeamId().equals(e.team().id()))
                    .map(e -> e.player() != null ? e.player().name() : null)
                    .filter(n -> n != null && !n.isBlank())
                    .collect(Collectors.joining(", "));

            String awayNames = events.response().stream()
                    .filter(e -> "Goal".equalsIgnoreCase(e.type()))
                    .filter(e -> e.team() != null && match.getAwayTeamId() != null
                            && match.getAwayTeamId().equals(e.team().id()))
                    .map(e -> e.player() != null ? e.player().name() : null)
                    .filter(n -> n != null && !n.isBlank())
                    .collect(Collectors.joining(", "));

            // Empty-string sentinel pour éviter de re-fetch un match 0-0 (pas de buteurs mais déjà traité)
            match.setHomeScorers(homeNames.isEmpty() ? "" : homeNames);
            match.setAwayScorers(awayNames.isEmpty() ? "" : awayNames);
            matchRepository.save(match);
        }
    }

    private List<FixturesApiFootballResponse.FixtureData> fetchFixtures(int season) {
        FixturesApiFootballResponse response = apiFootballClient.getMatch(idTeamSenegal, season);
        return (response.response() != null) ? response.response() : List.of();
    }

    private void syncFixtures(List<MatchEntity> matches) {
        matches.forEach(m ->
                matchRepository.findByIdFixture(m.getIdFixture())
                        .ifPresentOrElse(
                                existing -> {
                                    existing.setTimestamp(m.getTimestamp());
                                    existing.setDate(m.getDate());
                                    existing.setReferee(m.getReferee());
                                    existing.setHomeTeamId(m.getHomeTeamId());
                                    existing.setHomeName(m.getHomeName());
                                    existing.setHomeLogo(m.getHomeLogo());
                                    existing.setAwayTeamId(m.getAwayTeamId());
                                    existing.setAwayName(m.getAwayName());
                                    existing.setAwayLogo(m.getAwayLogo());
                                    existing.setHomeGoals(m.getHomeGoals());
                                    existing.setAwayGoals(m.getAwayGoals());
                                    existing.setStatusShort(m.getStatusShort());
                                    existing.setCompetition(m.getCompetition());
                                    matchRepository.save(existing);
                                },
                                () -> matchRepository.save(m)
                        )
        );
    }

    private void evictCache() {
        Cache cache = cacheManager.getCache("matches");
        if (cache != null) {
            cache.clear();
        }
    }

    private void saveIntoCache(List<MatchsResponse> matchsResponse) {
        Cache cache = cacheManager.getCache("matches");
        if (cache != null) {
            cache.put("all", matchsResponse);
        }
    }

    private int resolveSeason() {
        if (configuredSeason != null && !configuredSeason.isBlank()) {
            return Integer.parseInt(configuredSeason.trim());
        }
        return DateUtilities.currentSeason();
    }


}
