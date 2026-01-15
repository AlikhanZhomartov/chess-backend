package io.github.alikhanzhomartov.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alikhanzhomartov.exception.ErrorResponseDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResponseUtils {
    private static final Logger log = LoggerFactory.getLogger(ResponseUtils.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private ResponseUtils() {
    }

    public static void sendJson(ChannelHandlerContext ctx, Object response) {
        try {
            String json = mapper.writeValueAsString(response);

            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(new TextWebSocketFrame(json));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response object: {}", response.getClass().getSimpleName(), e);
            sendError(ctx, "INTERNAL_ERROR", "Serialization failure", null);
        }
    }

    public static void sendError(ChannelHandlerContext ctx, String code, String message, String requestId) {
        try {
            ErrorResponseDto errorDto = new ErrorResponseDto(code, message, requestId);
            String json = mapper.writeValueAsString(errorDto);

            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(new TextWebSocketFrame(json));
            }
        } catch (Exception e) {
            log.error("Error sending error response: {}", e.getMessage());
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
