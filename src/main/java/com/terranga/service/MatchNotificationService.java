package com.terranga.service;

import com.terranga.dto.NotificationRequest;
import com.terranga.entities.MatchEntity;
import com.terranga.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Évalue et envoie les 3 notifications planifiées par match :
 *  1. Matin du match (08:00 timezone système)
 *  2. Pré-match (timestamp - 10 min)
 *  3. Post-match (timestamp + 110 min ≈ fin de match + 10 min de marge)
 *
 * Idempotence via 3 flags booléens stockés sur MatchEntity.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MatchNotificationService {

    private static final long PRE_MATCH_OFFSET_SECONDS = 10L * 60L;            // 10 min avant KO
    private static final long PRE_MATCH_GRACE_AFTER_KO_SECONDS = 30L * 60L;    // ne pas envoyer si > 30 min après KO
    private static final long POST_MATCH_OFFSET_SECONDS = 110L * 60L;          // 110 min après KO
    private static final int MORNING_HOUR = 8;                                  // 08:00
    private static final long LOOKBACK_SECONDS = 4L * 3600L;                    // -4h pour rattraper post-match
    private static final long LOOKAHEAD_SECONDS = 30L * 3600L;                  // +30h pour catch matin du lendemain

    private final MatchRepository matchRepository;
    private final FirebaseMessagingService firebaseMessagingService;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter STORED_DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transactional
    public void processDueNotifications() {
        long now = System.currentTimeMillis() / 1000;
        long lowerTs = now - LOOKBACK_SECONDS;
        long upperTs = now + LOOKAHEAD_SECONDS;

        List<MatchEntity> candidates = matchRepository.findNotificationCandidates(lowerTs, upperTs);
        if (candidates.isEmpty()) return;

        log.debug("Scheduler notifs : {} match(s) candidat(s)", candidates.size());
        for (MatchEntity m : candidates) {
            processMatchNotifications(m, now);
        }
    }

    private void processMatchNotifications(MatchEntity m, long now) {
        if (m.getTimestamp() == null) return;
        long matchTs = m.getTimestamp();

        // 1. Notif matinale (08:00 le jour du match)
        if (notSent(m.getMorningNotifSent())) {
            long morningTrigger = computeMorningTriggerTs(matchTs);
            // Fenêtre : entre 08:00 et l'heure du coup d'envoi
            if (now >= morningTrigger && now < matchTs) {
                sendNotif(m, "morning_match_day",
                        "🦁 Match aujourd'hui",
                        String.format("%s vs %s à %s",
                                m.getHomeName(), m.getAwayName(), extractTime(m.getDate())));
                m.setMorningNotifSent(true);
                matchRepository.save(m);
            }
        }

        // 2. Notif pré-match (timestamp - 10 min)
        if (notSent(m.getPreMatchNotifSent())) {
            long preTrigger = matchTs - PRE_MATCH_OFFSET_SECONDS;
            // Fenêtre : entre (KO - 10 min) et (KO + 30 min) — pas de rattrapage trop tard
            if (now >= preTrigger && now < matchTs + PRE_MATCH_GRACE_AFTER_KO_SECONDS) {
                sendNotif(m, "pre_match",
                        "⚽ Coup d'envoi dans 10 min",
                        String.format("%s vs %s", m.getHomeName(), m.getAwayName()));
                m.setPreMatchNotifSent(true);
                matchRepository.save(m);
            }
        }

        // 3. Notif post-match (timestamp + 110 min)
        if (notSent(m.getPostMatchNotifSent())) {
            long postTrigger = matchTs + POST_MATCH_OFFSET_SECONDS;
            if (now >= postTrigger) {
                sendNotif(m, "post_match",
                        "🏁 Match terminé",
                        String.format("%s vs %s", m.getHomeName(), m.getAwayName()));
                m.setPostMatchNotifSent(true);
                matchRepository.save(m);
            }
        }
    }

    /** Une notif est considérée "à envoyer" si flag est null (ancienne ligne) ou false. */
    private static boolean notSent(Boolean flag) {
        return flag == null || !flag;
    }

    private void sendNotif(MatchEntity m, String type, String title, String body) {
        log.info("Notif {} pour match {} (idFixture={}) : {}",
                type, m.getId(), m.getIdFixture(), body);
        NotificationRequest req = new NotificationRequest(title, body, Map.of(
                "type", type,
                "matchId", String.valueOf(m.getIdFixture())
        ));
        firebaseMessagingService.sendNotificationToAll(req);
    }

    /** Timestamp Unix de 08:00:00 du jour où tombe le match (timezone système). */
    private long computeMorningTriggerTs(long matchTs) {
        LocalDate matchDay = Instant.ofEpochSecond(matchTs)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return matchDay.atTime(MORNING_HOUR, 0)
                .atZone(ZoneId.systemDefault())
                .toEpochSecond();
    }

    /** Extrait "HH:mm" depuis le champ date stocké "DD/MM/YYYY HH:mm". */
    private String extractTime(String storedDate) {
        if (storedDate == null) return "?";
        try {
            return LocalDateTime.parse(storedDate, STORED_DATE_FMT).format(TIME_FMT);
        } catch (Exception e) {
            return "?";
        }
    }
}
