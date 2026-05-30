package com.terranga.controller;

import com.terranga.dto.FcmTokenRequest;
import com.terranga.dto.FcmTokenResponse;
import com.terranga.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/fcm-tokens")
@RequiredArgsConstructor
@Slf4j
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    /** Le mobile appelle cet endpoint au démarrage avec son token Firebase. */
    @PostMapping
    public ResponseEntity<FcmTokenResponse> register(@RequestBody FcmTokenRequest request) {
        log.info("============= POST /fcm-tokens =============");
        return ResponseEntity.ok(fcmTokenService.registerToken(request));
    }

    /** Désinscription (logout / désinstallation côté mobile). */
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deactivate(@PathVariable String token) {
        log.info("============= DELETE /fcm-tokens =============");
        fcmTokenService.deactivateToken(token);
        return ResponseEntity.noContent().build();
    }
}
