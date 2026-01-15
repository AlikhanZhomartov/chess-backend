package io.github.alikhanzhomartov.config;

public class SecurityConfig {
    public static final String JWT_SECRET =
            System.getenv().getOrDefault("JWT_SECRET", "chess-secret-key");
}
