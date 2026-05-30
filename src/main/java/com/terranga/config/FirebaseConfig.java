package com.terranga.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * Initialise Firebase Admin SDK au démarrage de l'app.
 * Le fichier service-account JSON doit être placé dans src/main/resources/.
 * Si le fichier est absent, on log un warning mais l'app continue de tourner
 * (les fonctionnalités FCM seront simplement HS).
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-file:firebase-service-account.json}")
    private String serviceAccountFile;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp déjà initialisé");
            return;
        }
        try {
            ClassPathResource resource = new ClassPathResource(serviceAccountFile);
            if (!resource.exists()) {
                log.warn("⚠ Fichier Firebase {} introuvable dans classpath — FCM désactivé. " +
                        "Placez votre JSON service-account dans src/main/resources/", serviceAccountFile);
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("✓ FirebaseApp initialisé (credentials : {})", serviceAccountFile);
        } catch (IOException e) {
            log.error("Échec d'initialisation Firebase", e);
        }
    }
}
