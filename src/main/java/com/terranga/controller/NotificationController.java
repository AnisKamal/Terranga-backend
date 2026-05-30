package com.terranga.controller;

import com.terranga.dto.NotificationRequest;
import com.terranga.dto.NotificationResponse;
import com.terranga.service.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint manuel de déclenchement des notifications.
 * ⚠ Pour MVP, endpoint ouvert. À sécuriser (auth admin) avant prod.
 */
@RestController
@RequestMapping("api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final FirebaseMessagingService firebaseMessagingService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@RequestBody NotificationRequest request) {
        log.info("============= POST /notifications/send : \"{}\" =============", request.title());
        return ResponseEntity.ok(firebaseMessagingService.sendNotificationToAll(request));
    }
}
