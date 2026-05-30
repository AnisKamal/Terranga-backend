package com.terranga.events;

/**
 * Émis quand un nouveau match (jamais vu) est inséré en DB lors de la sync API-Football,
 * ET que ce match est dans le futur.
 *
 * Le listener {@code NewMatchNotificationListener} l'utilise pour broadcaster une notif
 * FCM "Nouveau match annoncé" à tous les tokens actifs, après commit de la transaction.
 */
public record NewMatchInsertedEvent(
        Long idFixture,
        String homeName,
        String awayName,
        /** Format brut depuis MatchEntity : "DD/MM/YYYY HH:mm" — le listener formate. */
        String date,
        /** Unix seconds du coup d'envoi — utile pour double-check côté listener. */
        Long timestamp
) {}
