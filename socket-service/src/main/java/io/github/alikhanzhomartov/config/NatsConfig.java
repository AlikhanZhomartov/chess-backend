package io.github.alikhanzhomartov.config;

import io.nats.client.Connection;
import io.nats.client.Nats;

public class NatsConfig {

    public static Connection createNatsConnection(String url) {
        try {
            return Nats.connect(url);
        } catch (Exception e) {
            throw new RuntimeException("NATS connection failed", e);
        }
    }
}
