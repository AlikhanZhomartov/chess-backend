package io.github.alikhanzhomartov.service.impl;

import io.github.alikhanzhomartov.dto.response.GameStateResponseDto;
import io.github.alikhanzhomartov.dto.response.MakeMoveResponseDto;
import io.github.alikhanzhomartov.grpc.GetGameStateRequest;
import io.github.alikhanzhomartov.grpc.MakeMoveRequest;
import io.github.alikhanzhomartov.infrastructure.GrpcChannelPool;
import io.github.alikhanzhomartov.mapper.GameProtoMapper;
import io.github.alikhanzhomartov.service.GameEventPublisher;
import io.github.alikhanzhomartov.service.GameService;

public class GrpcGameServiceImpl implements GameService {

    private final GrpcChannelPool grpcChannelPool;
    private final GameEventPublisher publisher;

    public GrpcGameServiceImpl(GrpcChannelPool grpcChannelPool,
                               GameEventPublisher publisher) {
        this.grpcChannelPool = grpcChannelPool;
        this.publisher = publisher;
    }

    @Override
    public MakeMoveResponseDto processMove(String gameId, String userId, String move, String requestId) {
        var request = MakeMoveRequest.newBuilder()
                .setGameId(gameId)
                .setUserId(userId)
                .setMoveUci(move)
                .setRequestId(requestId)
                .build();

        var response = GameProtoMapper.mapToMoveResponse(
                requestId,
                grpcChannelPool.nextStub().makeMove(request)
        );

        if (response.success()) {
            publisher.publishMove(gameId, userId, response);
        }

        return response;
    }

    @Override
    public GameStateResponseDto getInitialState(String gameId, String userId) {
        var request = GetGameStateRequest.newBuilder()
                .setGameId(gameId)
                .setUserId(userId)
                .build();

        return GameProtoMapper.mapToInitResponse(
                grpcChannelPool.nextStub().getGameState(request)
        );
    }
}
