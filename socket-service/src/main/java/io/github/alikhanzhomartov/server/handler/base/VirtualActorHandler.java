package io.github.alikhanzhomartov.server.handler.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCounted;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class VirtualActorHandler<T> extends SimpleChannelInboundHandler<T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final int CHUNK_SIZE = 32;

    private final ExecutorService executor;
    private final Queue<Runnable> mailbox;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    protected VirtualActorHandler(ExecutorService executor) {
        this.executor = executor;
        this.mailbox = new MpscUnboundedArrayQueue<>(CHUNK_SIZE);
    }

    protected abstract void process(ChannelHandlerContext ctx, T message);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T message) {
        if (message instanceof ReferenceCounted rc) {
            rc.retain();
        }

        mailbox.offer(() -> process(ctx, message));

        scheduleExecution();
    }

    private void scheduleExecution() {
        if (isRunning.compareAndSet(false, true)) {
            executor.submit(this::drainLoop);
        }
    }

    private void drainLoop() {
        try {
            Runnable task;
            while ((task = mailbox.poll()) != null) {
                try {
                    task.run();
                } catch (Throwable t) {
                    log.error("Actor task execution failed", t);
                }
            }
        } finally {
            isRunning.set(false);
            if (!mailbox.isEmpty()) {
                scheduleExecution();
            }
        }
    }
}
