package io.github.alikhanzhomartov.dto.common;

public record GameStateDto(
        String fen,
        int movePly,
        long whiteTimeMs,
        long blackTimeMs,
        String status,
        String endReason,
        boolean isWhiteTurn
) {
}
