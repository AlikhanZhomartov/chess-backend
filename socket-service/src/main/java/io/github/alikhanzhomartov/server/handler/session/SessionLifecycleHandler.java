package io.github.alikhanzhomartov.server.handler.session;

import io.github.alikhanzhomartov.config.ConcurrencyConfig;
import io.github.alikhanzhomartov.dto.response.GameStateResponseDto;
import io.github.alikhanzhomartov.service.GameService;
import io.github.alikhanzhomartov.session.SessionContext;
import io.github.alikhanzhomartov.session.SessionManager;
import io.github.alikhanzhomartov.utils.ResponseUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static io.github.alikhanzhomartov.constant.AttributeKeyConstants.*;
import static io.github.alikhanzhomartov.utils.ResponseUtils.sendError;

public class SessionLifecycleHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SessionLifecycleHandler.class);

    private final GameService gameService;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public SessionLifecycleHandler(GameService gameService,
                                   SessionManager sessionManager) {
        this.gameService = gameService;
        this.sessionManager = sessionManager;
        this.executor = ConcurrencyConfig.V_THREAD_EXECUTOR;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            ctx.channel().attr(IS_WEBSOCKET).set(true);

            String userId = ctx.channel().attr(USER_ID).get();
            String gameId = ctx.channel().attr(GAME_ID).get();

            if (userId != null && gameId != null) {

                SessionContext sessionContext = new SessionContext(
                        userId,
                        gameId,
                        ctx.channel()
                );
                sessionManager.registerSession(sessionContext);

                executor.submit(() -> {
                    try {
                        GameStateResponseDto response = gameService.getInitialState(gameId, userId);
                        ResponseUtils.sendJson(ctx, response);
                    } catch (Exception e) {
                        log.error("Init failed", e);
                        sendError(ctx, "INIT_FAILED", "Could not load game state", null);
                    }
                });
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManager.removeSession(ctx.channel().id());
        super.channelInactive(ctx);
    }
}
