package com.terranga.scheduler;

import com.terranga.service.FixturesService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchSyncScheduler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MatchSyncScheduler.class);

    private final FixturesService fixturesService;

    @Override
    public void run(ApplicationArguments args) {
        sync();
    }

    // Toutes les heures : 24 appels/jour max, bien sous le quota de 100.
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledSync() {
        sync();
    }

    private void sync() {
        try {
            fixturesService.UpdateMatch();
        } catch (Exception e) {
            log.error("Échec de la synchronisation des matchs : {}", e.getMessage());
        }
    }
}
