package com.terranga.scheduler;

import com.terranga.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsSyncScheduler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NewsSyncScheduler.class);

    private final NewsService newsService;

    @Override
    public void run(ApplicationArguments args) {
        sync();
    }

    /** Toutes les heures : les actualités bougent vite. */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledSync() {
        sync();
    }

    private void sync() {
        try {
            newsService.syncArticles();
        } catch (Exception e) {
            log.error("Échec de la synchronisation des actualités : {}", e.getMessage());
        }
    }
}
