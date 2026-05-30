package com.terranga.listener;

import com.terranga.dto.NotificationRequest;
import com.terranga.events.NewMatchInsertedEvent;
import com.terranga.service.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Écoute les {@link NewMatchInsertedEvent} et envoie une notification FCM à tous les tokens actifs.
 *
 * Activé uniquement APRÈS commit de la transaction DB ({@code AFTER_COMMIT}) :
 * si la transaction rollback (rare), aucune notif n'est envoyée → cohérence garantie.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NewMatchNotificationListener {

    private final FirebaseMessagingService firebaseMessagingService;

    /** Format stocké en DB par MatchMapper.formatDate(...) */
    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Format lisible côté notif : "1 juin 2026, 18:30" */
    private static final DateTimeFormatter OUTPUT_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.FRENCH);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNewMatch(NewMatchInsertedEvent event) {
        try {
            String body = String.format(
                    "%s vs %s — %s",
                    event.homeName(),
                    event.awayName(),
                    formatDate(event.date())
            );

            NotificationRequest req = new NotificationRequest(
                    "🦁 Nouveau match annoncé",
                    body,
                    Map.of(
                            "type", "new_match",
                            "matchId", String.valueOf(event.idFixture())
                    )
            );

            log.info("Broadcast notif nouveau match : {}", body);
            firebaseMessagingService.sendNotificationToAll(req);
        } catch (Exception e) {
            // Une erreur d'envoi de notif ne doit JAMAIS impacter la sync DB
            log.error("Échec envoi notif nouveau match (id={})", event.idFixture(), e);
        }
    }

    private static String formatDate(String raw) {
        if (raw == null || raw.isBlank()) return "date inconnue";
        try {
            return LocalDateTime.parse(raw, INPUT_FMT).format(OUTPUT_FMT);
        } catch (Exception e) {
            log.debug("Date non parsable pour la notif : {}", raw);
            return raw;
        }
    }
}
