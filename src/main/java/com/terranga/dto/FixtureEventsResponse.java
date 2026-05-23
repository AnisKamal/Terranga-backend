package com.terranga.dto;

import java.util.List;

/**
 * Réponse de l'endpoint API-Football /fixtures/events?fixture={id}.
 * Contient tous les événements d'un match (buts, cartons, substitutions...).
 * On ne filtre que les events de type "Goal" côté service.
 */
public record FixtureEventsResponse(List<EventData> response) {

    public record EventData(TimeInfo time, EventTeam team, EventPlayer player, String type, String detail) {}

    public record TimeInfo(Integer elapsed, Integer extra) {}

    public record EventTeam(Long id, String name) {}

    public record EventPlayer(Long id, String name) {}
}
