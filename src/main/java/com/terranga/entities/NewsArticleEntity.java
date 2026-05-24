package com.terranga.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_news_article")
@Setter
@Getter
public class NewsArticleEntity extends BaseEntity {

    /** Identifiant unique RSS — sert de clé de déduplication */
    @Column(unique = true, nullable = false, length = 500)
    private String guid;

    @Column(length = 500)
    private String title;

    @Column(length = 1000)
    private String link;

    /** URL de l'image extraite du HTML description (nullable) */
    @Column(length = 1000)
    private String imageUrl;

    /** Nom du média source (ex: "Wiwsport", "L'Équipe") */
    private String source;

    /** Unix timestamp en secondes pour tri SQL natif efficace */
    private Long publishedTimestamp;

    /** Résumé court (texte plat, HTML strippé) extrait du <description> WordPress. */
    @Column(length = 600)
    private String excerpt;
}
