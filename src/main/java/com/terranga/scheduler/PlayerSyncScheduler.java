package com.terranga.scheduler;

import com.terranga.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerSyncScheduler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PlayerSyncScheduler.class);

    private final PlayerService playerService;

    @Override
    public void run(ApplicationArguments args) {
        sync();
    }

    /** Tous les lundis à 3h : le squad change peu, hebdomadaire est largement suffisant. */
    @Scheduled(cron = "0 0 3 * * MON")
    public void scheduledSync() {
        sync();
    }

    private void sync() {
        try {
            playerService.syncSquad();
        } catch (Exception e) {
            log.error("Échec de la synchronisation du squad : {}", e.getMessage());
        }
    }
}
