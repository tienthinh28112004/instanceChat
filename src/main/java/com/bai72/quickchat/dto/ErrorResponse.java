package com.bai72.quickchat.dto;

import java.time.Instant;

public record ErrorResponse(ErrorBody error) {
    public record ErrorBody(String code, String message, Instant timestamp) {
    }
}
