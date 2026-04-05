package com.zorvyn.finance.app.dtos.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ApiError(
        String path,
        String message,
        int statusCode,
        LocalDateTime timestamp,
        Map<String, String> errors
) {
}
