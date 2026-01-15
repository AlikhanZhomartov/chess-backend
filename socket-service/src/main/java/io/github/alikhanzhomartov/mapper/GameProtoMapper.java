package io.github.alikhanzhomartov.mapper;

import io.github.alikhanzhomartov.dto.common.GameStateDto;
import io.github.alikhanzhomartov.dto.response.GameStateResponseDto;
import io.github.alikhanzhomartov.dto.response.MakeMoveResponseDto;
import io.github.alikhanzhomartov.grpc.GameState;
import io.github.alikhanzhomartov.grpc.GetGameStateResponse;
import io.github.alikhanzhomartov.grpc.MakeMoveResponse;
import io.github.alikhanzhomartov.grpc.MoveInvalidReason;

public final class GameProtoMapper {

    private GameProtoMapper() {
    }

    public static MakeMoveResponseDto mapToMoveResponse(String requestId, MakeMoveResponse proto) {
        return new MakeMoveResponseDto(
                "MAKE_MOVE",
                requestId,
                proto.getSuccess(),
                mapError(proto.getInvalidReason()),
                mapToGameStateDto(proto.getGameState())
        );
    }

    public static GameStateResponseDto mapToInitResponse(GetGameStateResponse proto) {
        return new GameStateResponseDto(
                "GAME_STATE",
                mapToGameStateDto(proto.getGameState())
        );
    }

    private static GameStateDto mapToGameStateDto(GameState proto) {
        if (proto == null) {
            return null;
        }

        return new GameStateDto(
                proto.getFen(),
                proto.getMovePly(),
                proto.getWhiteTimeMs(),
                proto.getBlackTimeMs(),
                proto.getStatus().name(),
                proto.getEndReason().name(),
                proto.getIsWhiteTurn()
        );
    }

    private static String mapError(MoveInvalidReason reason) {
        if (reason == null
                || reason == MoveInvalidReason.MOVE_INVALID_REASON_NONE
                || reason == MoveInvalidReason.MOVE_INVALID_REASON_UNSPECIFIED) {
            return null;
        }
        return reason.name();
    }
}
