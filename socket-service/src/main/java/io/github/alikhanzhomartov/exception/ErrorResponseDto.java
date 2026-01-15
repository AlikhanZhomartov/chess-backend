package io.github.alikhanzhomartov.exception;

import io.github.alikhanzhomartov.dto.response.ServerResponse;

public record ErrorResponseDto(
        String type,
        String code,
        String message,
        String requestId
) implements ServerResponse {
    public ErrorResponseDto(String code, String message, String requestId) {
        this("ERROR", code, message, requestId);
    }
}