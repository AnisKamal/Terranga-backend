package com.terranga.dto;

/** Payload d'enregistrement d'un token FCM. */
public record FcmTokenRequest(String token, String deviceInfo) {}
