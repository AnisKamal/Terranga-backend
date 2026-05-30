package com.terranga.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_match")
@Setter
@Getter
public class MatchEntity extends BaseEntity{

    @Column(unique = true, nullable = false)
    private Long idFixture;

    private Long timestamp;

    private String date ;

    private String referee ;

    private String homeName;

    private String homeLogo;

    private String awayName;

    private String awayLogo;

    private Integer homeGoals;

    private Integer awayGoals;

    private String statusShort;

    private Long homeTeamId;

    private Long awayTeamId;

    /** Liste des buteurs de l'équipe domicile, séparés par virgule. Null tant que pas synchronisé via /fixtures/events. */
    @Column(length = 1000)
    private String homeScorers;

    @Column(length = 1000)
    private String awayScorers;

    /** Nom de la compétition (ex: "Africa Cup of Nations") ou "Match amical" pour les Friendlies. */
    private String competition;

    /** Notif "Match aujourd'hui" envoyée à 8h00 le jour du match. null = pas encore envoyée. */
    private Boolean morningNotifSent = false;

    /** Notif "Coup d'envoi dans 10 min" envoyée à timestamp - 10 min. null = pas encore envoyée. */
    private Boolean preMatchNotifSent = false;

    /** Notif "Match terminé" envoyée à timestamp + 110 min. null = pas encore envoyée. */
    private Boolean postMatchNotifSent = false;

}
