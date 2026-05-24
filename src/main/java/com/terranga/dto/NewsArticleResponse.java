package com.terranga.dto;

/**
 * DTO mobile pour un article de presse.
 * publishedAt est en format ISO-8601 pour formatage côté mobile.
 */
public record NewsArticleResponse(
        String guid,
        String title,
        String link,
        String imageUrl,
        String source,
        String publishedAt,
        String excerpt
) {}
