package com.terranga.dto;

import java.util.Map;

/**
 * Payload pour déclencher une notification push.
 * `data` optionnel — sert pour le deep-linking (ex: { "screen": "match", "id": "123" }).
 */
public record NotificationRequest(
        String title,
        String body,
        Map<String, String> data
) {}
