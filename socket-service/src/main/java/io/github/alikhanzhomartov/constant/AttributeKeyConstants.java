package io.github.alikhanzhomartov.constant;

import io.netty.util.AttributeKey;

public final class AttributeKeyConstants {
    private AttributeKeyConstants() {
    }

    public static final AttributeKey<String> USER_ID = AttributeKey.valueOf("USER_ID");
    public static final AttributeKey<String> GAME_ID = AttributeKey.valueOf("GAME_ID");
    public static final AttributeKey<Boolean> IS_WEBSOCKET = AttributeKey.valueOf("IS_WEBSOCKET");
}
