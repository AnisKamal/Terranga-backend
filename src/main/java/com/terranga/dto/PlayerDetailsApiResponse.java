package com.terranga.dto;

import java.util.List;

/**
 * Réponse de l'endpoint API-Football /players?id={playerId}&season={year}.
 * Contient les infos détaillées + stats de la saison pour un joueur.
 */
public record PlayerDetailsApiResponse(List<PlayerData> response) {

    public record PlayerData(PlayerInfo player, List<Statistic> statistics) {}

    public record PlayerInfo(
            Long id,
            String name,
            String firstname,
            String lastname,
            Integer age,
            Birth birth,
            String nationality,
            String height,
            String weight,
            Boolean injured,
            String photo
    ) {}

    public record Birth(String date, String place, String country) {}

    public record Statistic(Team team, League league, Games games, Goals goals) {}

    public record Team(Long id, String name, String logo) {}

    public record League(String name, String country) {}

    public record Games(Integer appearances, Integer lineups, Integer minutes, String position, String rating) {}

    public record Goals(Integer total, Integer assists) {}
}
