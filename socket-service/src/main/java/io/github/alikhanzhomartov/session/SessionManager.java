package io.github.alikhanzhomartov.session;

import io.netty.channel.ChannelId;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final ConcurrentHashMap<String, Set<SessionContext>> gameGroups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChannelId, SessionContext> channelToSession = new ConcurrentHashMap<>();

    public void registerSession(SessionContext session) {
        channelToSession.put(session.channel().id(), session);

        gameGroups.computeIfAbsent(session.gameId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(session);
    }

    public Set<SessionContext> getSessionsByGameId(String gameId) {
        return gameGroups.getOrDefault(gameId, Collections.emptySet());
    }

    public void removeSession(ChannelId channelId) {
        SessionContext sessionContext = channelToSession.remove(channelId);

        if (sessionContext != null) {
            String gameId = sessionContext.gameId();

            gameGroups.computeIfPresent(gameId, (key, sessions) -> {
                sessions.remove(sessionContext);

                if (sessions.isEmpty()) {
                    return null;
                }

                return sessions;
            });
        }
    }
}
