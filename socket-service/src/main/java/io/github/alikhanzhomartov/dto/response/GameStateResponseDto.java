package io.github.alikhanzhomartov.dto.response;

import io.github.alikhanzhomartov.dto.common.GameStateDto;

public record GameStateResponseDto(
        String type,
        GameStateDto state
) implements ServerResponse {
}
