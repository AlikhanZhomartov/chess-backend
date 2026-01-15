package io.github.alikhanzhomartov.service.impl;

import io.github.alikhanzhomartov.config.ConcurrencyConfig;
import io.github.alikhanzhomartov.service.GameEventListener;
import io.github.alikhanzhomartov.session.SessionContext;
import io.github.alikhanzhomartov.session.SessionManager;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class GameEventListenerImpl implements GameEventListener {
    private static final Logger log = LoggerFactory.getLogger(GameEventListenerImpl.class);

    private final Connection natsConnection;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public GameEventListenerImpl(Connection natsConnection, SessionManager sessionManager) {
        this.natsConnection = natsConnection;
        this.sessionManager = sessionManager;
        this.executor = ConcurrencyConfig.BROADCAST_EXECUTOR;
    }

    @Override
    public void start() {
        Dispatcher dispatcher = natsConnection.createDispatcher(this::handleMessage);

        dispatcher.subscribe("game.*.move");

        log.info("NATS Listener started on topic: game.*.move");
    }

    private void handleMessage(Message msg) {
        try {
            String topic = msg.getSubject();
            String gameId = extractGameId(topic);

            if (gameId == null) return;

            Set<SessionContext> localPlayers = sessionManager.getSessionsByGameId(gameId);

            if (localPlayers.isEmpty()) {
                return;
            }

            byte[] dataCopy = msg.getData().clone();
            String senderId = msg.getHeaders().getFirst("Sender-Id");

            executor.submit(() -> {
                TextWebSocketFrame frame = new TextWebSocketFrame(Unpooled.wrappedBuffer(dataCopy));
                try {
                    for (SessionContext player : localPlayers) {
                        if (!player.userId().equals(senderId)) {
                            player.send(frame.retainedDuplicate());
                        }
                    }
                } finally {
                    frame.release();
                }
            });
        } catch (Exception e) {
            log.error("Error processing NATS message: {}", msg, e);
        }
    }

    private String extractGameId(String topic) {
        int firstDot = topic.indexOf('.');
        int lastDot = topic.lastIndexOf('.');
        if (firstDot > 0 && lastDot > firstDot) {
            return topic.substring(firstDot + 1, lastDot);
        }
        return null;
    }
}
