package io.github.alikhanzhomartov.service;

import io.github.alikhanzhomartov.dto.response.ServerResponse;

public interface GameEventPublisher {
    void publishMove(String gameId, String userId, ServerResponse response);
}
