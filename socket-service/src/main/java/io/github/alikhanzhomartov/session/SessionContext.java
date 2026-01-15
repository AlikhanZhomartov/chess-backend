package io.github.alikhanzhomartov.session;

import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

public record SessionContext(
        String userId,
        String gameId,
        Channel channel
) {
    public void send(Object message) {
        if (channel.isActive() && channel.isWritable()) {
            channel.writeAndFlush(message);
        } else {
            ReferenceCountUtil.release(message);
        }
    }
}