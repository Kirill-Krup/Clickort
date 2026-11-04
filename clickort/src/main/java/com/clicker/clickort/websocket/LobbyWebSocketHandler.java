package com.clicker.clickort.websocket;

import com.clicker.clickort.dto.ClickEvent;
import com.clicker.clickort.dto.ServerMessageDto;
import com.clicker.clickort.exception.AlreadyJoinedException;
import com.clicker.clickort.exception.LobbyException;
import com.clicker.clickort.exception.NotJoinedException;
import com.clicker.clickort.service.GameService;
import com.clicker.clickort.service.KafkaMessengerService;
import com.clicker.clickort.service.LobbyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

/**
 * Client messages:
 * <pre>
 * {"type":"JOIN","nickname":"alice"}
 * {"type":"READY"}
 * {"type":"CLICK"}
 * </pre>
 * Closing the connection removes the player from the lobby and leaderboard.
 */
@Component
@RequiredArgsConstructor
public class LobbyWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LobbyWebSocketHandler.class);

    private final LobbyService lobby;
    private final WebSocketSessionRegistry registry;
    private final ObjectMapper mapper;
    private final GameService gameService;
    private final KafkaMessengerService<String, ClickEvent> kafkaMessengerService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.register(session);
        registry.sendTo(session.getId(), ServerMessageDto.lobbyState(lobby.state()));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String sessionId = session.getId();

        JsonNode root;
        try {
            root = mapper.readTree(message.getPayload());
        } catch (JacksonException e) {
            sendError(sessionId, "INVALID_MESSAGE", "Message is not valid JSON");
            return;
        }
        String type = text(root, "type");
        if (type == null) {
            sendError(sessionId, "INVALID_MESSAGE", "Message must be a JSON object with a \"type\" field");
            return;
        }

        try {
            switch (type) {
                case "JOIN" -> join(sessionId, text(root, "nickname"));
                case "READY" -> ready(sessionId);
                case "CLICK" -> handleClick(sessionId);
                default -> sendError(sessionId, "UNKNOWN_TYPE", "Unknown message type: " + type);
            }
        } catch (LobbyException e) {
            sendError(sessionId, e.code(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error handling message from session {}", sessionId, e);
            sendError(sessionId, "INTERNAL_ERROR", "Internal server error");
        }
    }

    private void handleClick(String sessionId) {
        String playerId = registry.playerId(sessionId).orElseThrow(NotJoinedException::new);
        gameService.registerClick(playerId);
        kafkaMessengerService.send(new ClickEvent("", playerId, "", Instant.now()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.remove(session.getId()).ifPresent(playerId -> {
            gameService.removePlayer(playerId);
            lobby.leave(playerId);
            broadcastState();
        });
    }

    private void join(String sessionId, String nickname) {
        if (registry.playerId(sessionId).isPresent()) {
            throw new AlreadyJoinedException();
        }
        String playerId = lobby.join(nickname);
        try {
            registry.bind(sessionId, playerId);
        } catch (IllegalStateException e) {
            lobby.leave(playerId);
            throw e;
        }
        registry.sendTo(sessionId, ServerMessageDto.joined(playerId));
        broadcastState();
    }

    private void ready(String sessionId) {
        String playerId = registry.playerId(sessionId).orElseThrow(NotJoinedException::new);
        lobby.ready(playerId);
        broadcastState();
    }

    private void broadcastState() {
        registry.broadcast(ServerMessageDto.lobbyState(lobby.state()));
    }

    private void sendError(String sessionId, String code, String message) {
        log.debug("Rejecting message from session {}: {} ({})", sessionId, code, message);
        registry.sendTo(sessionId, ServerMessageDto.error(code, message));
    }

    /** Returns the field as text, or null if the node is not an object or the field is missing or not a string. */
    private static String text(JsonNode root, String field) {
        if (root == null || !root.isObject()) {
            return null;
        }
        JsonNode value = root.get(field);
        return value != null && value.isTextual() ? value.asText() : null;
    }
}
