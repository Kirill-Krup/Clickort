package com.clicker.clickort.websocket;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import com.clicker.clickort.dto.ServerMessageDto;

/**
 * Keeps track of open WebSocket connections and which player each one belongs to.
 *
 * <p>A raw {@link WebSocketSession} must not be written to from several threads at once,
 * so every session is wrapped in a {@link ConcurrentWebSocketSessionDecorator}.
 * A slow client that cannot keep up is disconnected instead of growing memory without limit.
 */
@Component
public class WebSocketSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionRegistry.class);

    private static final int SEND_TIME_LIMIT_MS = 5_000;
    private static final int SEND_BUFFER_LIMIT_BYTES = 64 * 1024;

    private final ObjectMapper mapper;
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> playerBySession = new ConcurrentHashMap<>();

    public WebSocketSessionRegistry(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void register(WebSocketSession session) {
        sessions.put(session.getId(),
                new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT_MS, SEND_BUFFER_LIMIT_BYTES));
    }

    /** @throws IllegalStateException if the session was never registered (or is already gone) */
    public void bind(String sessionId, String playerId) {
        if (!sessions.containsKey(sessionId)) {
            throw new IllegalStateException("Unknown session: " + sessionId);
        }
        playerBySession.put(sessionId, playerId);
    }

    public Optional<String> playerId(String sessionId) {
        return Optional.ofNullable(playerBySession.get(sessionId));
    }

    /**
     * Forgets the session.
     *
     * @return the player that was bound to it, if any
     */
    public Optional<String> remove(String sessionId) {
        sessions.remove(sessionId);
        return Optional.ofNullable(playerBySession.remove(sessionId));
    }

    public void sendTo(String sessionId, ServerMessageDto message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null) {
            send(session, serialize(message));
        }
    }

    /** Sends to every open session. A failure on one connection does not affect the others. */
    public void broadcast(ServerMessageDto message) {
        String json = serialize(message);
        sessions.values().forEach(session -> send(session, json));
    }

    private void send(WebSocketSession session, String json) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException | RuntimeException e) {
            log.warn("Failed to send message to session {}: {}", session.getId(), e.toString());
        }
    }

    private String serialize(ServerMessageDto message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize " + message.type(), e);
        }
    }
}
