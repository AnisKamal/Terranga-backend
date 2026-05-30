package com.terranga.scheduler;

import com.terranga.service.MatchNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tick toutes les minutes pour évaluer si une notif planifiée doit partir.
 * Précision attendue : ±30 secondes par rapport au trigger théorique.
 */
@Component
@RequiredArgsConstructor
public class MatchNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchNotificationScheduler.class);

    private final MatchNotificationService matchNotificationService;

    /**
     * Toutes les 5 minutes (à :00, :05, :10, ...).
     * Compromis : 288 ticks/jour, jitter max 5 min sur le trigger pré-match
     * (acceptable pour une notif "10 min avant le match").
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void run() {
        try {
            matchNotificationService.processDueNotifications();
        } catch (Exception e) {
            log.error("Échec scheduler notifs matchs", e);
        }
    }
}
