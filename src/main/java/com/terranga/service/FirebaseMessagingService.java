package com.terranga.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.terranga.dto.NotificationRequest;
import com.terranga.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Envoi de notifications push via Firebase Admin SDK.
 * - Batch limité à 500 (limite Firebase)
 * - Désactivation auto des tokens invalides (UNREGISTERED / INVALID_ARGUMENT)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseMessagingService {

    private static final int FCM_BATCH_SIZE = 500;

    private final FcmTokenService fcmTokenService;

    /** Envoie une notification à tous les tokens actifs. */
    public NotificationResponse sendNotificationToAll(NotificationRequest request) {
        List<String> tokens = fcmTokenService.getAllActiveTokens();
        log.info("Envoi notif \"{}\" → {} token(s) actifs", request.title(), tokens.size());

        if (tokens.isEmpty()) {
            return new NotificationResponse(0, 0, List.of());
        }

        int totalSent = 0;
        int totalFailed = 0;
        List<String> allFailedTokens = new ArrayList<>();

        for (List<String> batch : partition(tokens, FCM_BATCH_SIZE)) {
            try {
                MulticastMessage message = buildMulticastMessage(batch, request);
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                totalSent += response.getSuccessCount();
                totalFailed += response.getFailureCount();
                handleFailedTokens(batch, response, allFailedTokens);
            } catch (FirebaseMessagingException e) {
                log.error("Échec envoi batch FCM (taille={})", batch.size(), e);
                totalFailed += batch.size();
                allFailedTokens.addAll(batch);
            } catch (IllegalStateException e) {
                // FirebaseApp non initialisé (fichier service-account manquant)
                log.error("FirebaseApp non disponible — vérifier le fichier service-account JSON", e);
                return new NotificationResponse(0, tokens.size(), tokens);
            }
        }

        log.info("Notif terminée : {} succès, {} échecs", totalSent, totalFailed);
        return new NotificationResponse(totalSent, totalFailed, allFailedTokens);
    }

    private MulticastMessage buildMulticastMessage(List<String> tokens, NotificationRequest request) {
        Map<String, String> data = request.data() != null ? request.data() : Map.of();
        return MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(request.title())
                        .setBody(request.body())
                        .build())
                .putAllData(data)
                .addAllTokens(tokens)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .build();
    }

    /** Désactive automatiquement les tokens invalides retournés par Firebase. */
    private void handleFailedTokens(List<String> batch, BatchResponse response, List<String> failedAcc) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse r = responses.get(i);
            if (r.isSuccessful()) continue;
            String token = batch.get(i);
            failedAcc.add(token);

            if (r.getException() == null) continue;
            MessagingErrorCode code = r.getException().getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                fcmTokenService.deactivateToken(token);
                log.info("Token désactivé (raison: {})", code);
            } else {
                log.warn("Échec token (raison: {}, msg: {})", code, r.getException().getMessage());
            }
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            out.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return out;
    }
}
