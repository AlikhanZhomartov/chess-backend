package io.github.alikhanzhomartov.service;

import io.github.alikhanzhomartov.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class GameBrainServiceImpl extends GameBrainServiceGrpc.GameBrainServiceImplBase {

    @Override
    public void makeMove(MakeMoveRequest request, StreamObserver<MakeMoveResponse> responseObserver) {
        sleep();

        GameState gameState = GameState.newBuilder()
                .setGameId(request.getGameId())
                .setFen("startpos")
                .setMovePly(1)
                .setWhiteTimeMs(300_000)
                .setBlackTimeMs(300_000)
                .setStatus(GameStatus.GAME_STATUS_ACTIVE)
                .setEndReason(GameEndReason.GAME_END_REASON_NONE)
                .setIsWhiteTurn(false)
                .build();

        MakeMoveResponse response = MakeMoveResponse.newBuilder()
                .setSuccess(true)
                .setInvalidReason(MoveInvalidReason.MOVE_INVALID_REASON_NONE)
                .setGameState(gameState)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getGameState(GetGameStateRequest request, StreamObserver<GetGameStateResponse> responseObserver) {
        sleep();

        GameState gameState = GameState.newBuilder()
                .setGameId(request.getGameId())
                .setFen("startpos")
                .setMovePly(1)
                .setWhiteTimeMs(300_000)
                .setBlackTimeMs(300_000)
                .setStatus(GameStatus.GAME_STATUS_ACTIVE)
                .setEndReason(GameEndReason.GAME_END_REASON_NONE)
                .setIsWhiteTurn(true)
                .build();

        GetGameStateResponse response = GetGameStateResponse.newBuilder()
                .setGameState(gameState)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
