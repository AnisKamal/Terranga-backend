package com.terranga.dto;

/**
 * DTO mobile pour l'écran détail joueur. Vue à plat des infos perso + stats saison
 * (extraites de la première entrée de statistics[] = club principal).
 */
public record PlayerDetailsResponse(
        Long id,
        String name,
        String firstname,
        String lastname,
        Integer age,
        String birthDate,
        String birthPlace,
        String nationality,
        String height,
        String weight,
        Boolean injured,
        String photo,
        String currentClub,
        String currentClubLogo,
        Integer appearances,
        Integer goals,
        Integer assists,
        Integer minutes
) {}
