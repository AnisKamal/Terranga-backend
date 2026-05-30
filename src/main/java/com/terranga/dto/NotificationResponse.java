package com.terranga.dto;

import java.util.List;

public record NotificationResponse(
        int sentCount,
        int failedCount,
        List<String> failedTokens
) {}
