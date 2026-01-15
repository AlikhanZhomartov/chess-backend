package io.github.alikhanzhomartov.server.handler.game;

import io.github.alikhanzhomartov.config.ConcurrencyConfig;
import io.github.alikhanzhomartov.dto.request.ClientRequest;
import io.github.alikhanzhomartov.dto.request.MakeMoveRequestDto;
import io.github.alikhanzhomartov.dto.response.MakeMoveResponseDto;
import io.github.alikhanzhomartov.server.handler.base.VirtualActorHandler;
import io.github.alikhanzhomartov.service.GameService;
import io.github.alikhanzhomartov.utils.ResponseUtils;
import io.grpc.StatusRuntimeException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;

import static io.github.alikhanzhomartov.constant.AttributeKeyConstants.GAME_ID;
import static io.github.alikhanzhomartov.constant.AttributeKeyConstants.USER_ID;
import static io.github.alikhanzhomartov.utils.ResponseUtils.sendError;
import static io.github.alikhanzhomartov.utils.ResponseUtils.sendJson;

public class GamePlayHandler extends VirtualActorHandler<TextWebSocketFrame> {

    private final GameService gameService;

    public GamePlayHandler(GameService gameService) {
        super(ConcurrencyConfig.V_THREAD_EXECUTOR);
        this.gameService = gameService;
    }

    @Override
    protected void process(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        ClientRequest message = null;
        try {
            String json = frame.text();
            message = ResponseUtils.getMapper().readValue(json, ClientRequest.class);

            dispatch(ctx, message);
        } catch (Exception e) {
            handleException(ctx, e, message);
        } finally {
            ReferenceCountUtil.release(frame);
        }
    }

    private void dispatch(ChannelHandlerContext ctx, ClientRequest message) {
        String userId = ctx.channel().attr(USER_ID).get();
        String gameId = ctx.channel().attr(GAME_ID).get();

        switch (message) {
            case MakeMoveRequestDto move -> handle(ctx, userId, gameId, move);
            default -> log.warn("Unknown command from user {}: {}", userId, message.getClass().getSimpleName());
        }
    }

    private void handle(ChannelHandlerContext context, String userId, String gameId, MakeMoveRequestDto message) {
        MakeMoveResponseDto response = gameService.processMove(gameId, userId, message.moveUci(), message.requestId());
        sendJson(context, response);
    }

    private void handleException(ChannelHandlerContext ctx, Exception e, ClientRequest msg) {
        log.error("Action failed for user {}: {}", ctx.channel().attr(USER_ID).get(), e.getMessage());

        String requestId = (msg != null) ? msg.requestId() : null;
        String errorCode = (e instanceof StatusRuntimeException) ? "SERVICE_UNAVAILABLE" : "INTERNAL_ERROR";

        sendError(ctx, errorCode, e.getMessage(), requestId);
    }
}
