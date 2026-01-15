package io.github.alikhanzhomartov.service;

import io.github.alikhanzhomartov.dto.response.GameStateResponseDto;
import io.github.alikhanzhomartov.dto.response.MakeMoveResponseDto;

public interface GameService {
    MakeMoveResponseDto processMove(String gameId, String userId, String move, String requestId);

    GameStateResponseDto getInitialState(String gameId, String userId);
}
