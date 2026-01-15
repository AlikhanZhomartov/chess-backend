package io.github.alikhanzhomartov.dto.request;

public record MakeMoveRequestDto(
        String requestId,
        String moveUci
) implements ClientRequest {
}
