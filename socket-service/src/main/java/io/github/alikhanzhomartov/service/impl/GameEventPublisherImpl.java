package io.github.alikhanzhomartov.service.impl;

import io.github.alikhanzhomartov.dto.response.ServerResponse;
import io.github.alikhanzhomartov.service.GameEventPublisher;
import io.github.alikhanzhomartov.utils.ResponseUtils;
import io.nats.client.Connection;
import io.nats.client.impl.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEventPublisherImpl implements GameEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(GameEventPublisherImpl.class);

    private final Connection natsConnection;

    public GameEventPublisherImpl(Connection natsConnection) {
        this.natsConnection = natsConnection;
    }

    @Override
    public void publishMove(String gameId, String userId, ServerResponse response) {
        try {
            String topic = "game." + gameId + ".move";
            byte[] payload = ResponseUtils.getMapper().writeValueAsBytes(response);

            Headers headers = new Headers();
            headers.add("Sender-Id", userId);

            natsConnection.publish(topic, headers, payload);
        } catch (Exception e) {
            log.error("Error publishing move", e);
        }
    }
}
