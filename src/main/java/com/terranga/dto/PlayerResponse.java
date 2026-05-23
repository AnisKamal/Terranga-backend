package com.terranga.dto;

/**
 * DTO mobile pour la liste de joueurs (vue compacte).
 */
public record PlayerResponse(
        Long id,
        String name,
        Integer age,
        Integer number,
        String position,
        String photo
) {}
