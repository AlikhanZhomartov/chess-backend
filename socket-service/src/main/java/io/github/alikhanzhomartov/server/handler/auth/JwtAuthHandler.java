package io.github.alikhanzhomartov.server.handler.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.alikhanzhomartov.auth.JwtService;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.alikhanzhomartov.constant.AttributeKeyConstants.GAME_ID;
import static io.github.alikhanzhomartov.constant.AttributeKeyConstants.USER_ID;

public class JwtAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthHandler.class);

    private final JwtService jwtService;

    public JwtAuthHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        String authorization = fullHttpRequest.headers().getAsString(HttpHeaderNames.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            sendError(channelHandlerContext, HttpResponseStatus.UNAUTHORIZED);
            return;
        }

        try {
            String token = authorization.substring(7);
            DecodedJWT jwt = jwtService.verifyToken(token);

            String userId = jwt.getSubject();
            String gameId = jwt.getClaim("gameId").asString();

            if (userId == null || gameId == null) {
                sendError(channelHandlerContext, HttpResponseStatus.UNAUTHORIZED);
                return;
            }

            channelHandlerContext.channel().attr(USER_ID).set(userId);
            channelHandlerContext.channel().attr(GAME_ID).set(gameId);

            fullHttpRequest.retain();
            channelHandlerContext.fireChannelRead(fullHttpRequest);
        } catch (Exception e) {
            log.error("JWT Verification failed", e);
            sendError(channelHandlerContext, HttpResponseStatus.UNAUTHORIZED);
        }
    }

    private void sendError(ChannelHandlerContext context, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
