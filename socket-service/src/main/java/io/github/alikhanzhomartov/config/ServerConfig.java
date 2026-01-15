package io.github.alikhanzhomartov.config;

public record ServerConfig(
        int port,
        String grpcHost,
        int grpcPort,
        int grpcPoolSize,
        int bossThreads,
        int workerThreads,
        String natsUrl
) {
    public static ServerConfig loadFromEnv() {
        return new ServerConfig(
                getInt("SERVER_PORT", 8080),
                getStr("GRPC_HOST", "localhost"),
                getInt("GRPC_PORT", 9090),
                getInt("GRPC_POOL_SIZE", 16),
                getInt("NETTY_BOSS_THREADS", 1),
                getInt("NETTY_WORKER_THREADS", 0),
                getStr("NATS_URL", "nats://localhost:4222")
        );
    }

    private static String getStr(String key, String def) {
        return System.getenv().getOrDefault(key, def);
    }

    private static int getInt(String key, int def) {
        String val = System.getenv(key);
        if (val == null) return def;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid configuration for " + key + ": " + val);
        }
    }
}
