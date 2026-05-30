package com.terranga.dto;

import java.time.LocalDateTime;

public record FcmTokenResponse(
        Long id,
        String token,
        String deviceInfo,
        Boolean isActive,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}
