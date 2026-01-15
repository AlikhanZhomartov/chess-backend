package io.github.alikhanzhomartov.dto.response;

import io.github.alikhanzhomartov.dto.common.GameStateDto;

public record MakeMoveResponseDto(
        String type,
        String requestId,
        boolean success,
        String error,
        GameStateDto state
) implements ServerResponse {
}
