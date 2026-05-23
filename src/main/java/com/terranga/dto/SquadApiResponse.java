package com.terranga.dto;

import java.util.List;

/**
 * Réponse de l'endpoint API-Football /players/squads?team={id}.
 * Contient le squad officiel d'une équipe nationale (~25 joueurs).
 */
public record SquadApiResponse(List<SquadEntry> response) {

    public record SquadEntry(Team team, List<Player> players) {}

    public record Team(Long id, String name, String logo) {}

    public record Player(Long id, String name, Integer age, Integer number, String position, String photo) {}
}
