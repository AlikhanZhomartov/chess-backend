package io.github.alikhanzhomartov.exception;

import io.github.alikhanzhomartov.utils.ResponseUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.alikhanzhomartov.constant.AttributeKeyConstants.IS_WEBSOCKET;

public class GlobalExceptionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Critical pipeline error: ", cause);

        if (Boolean.TRUE.equals(ctx.channel().attr(IS_WEBSOCKET).get())) {
            String message = cause.getMessage() != null ? cause.getMessage() : "Unknown server error";
            ResponseUtils.sendError(ctx, "INTERNAL_ERROR", message, null);
        } else {
            ctx.close();
        }
    }
}
