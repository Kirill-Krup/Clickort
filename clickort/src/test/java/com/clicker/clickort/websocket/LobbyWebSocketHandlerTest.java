package com.clicker.clickort.websocket;

import com.clicker.clickort.config.LobbyProperties;
import com.clicker.clickort.repository.InMemoryLobbyRepository;
import com.clicker.clickort.service.LobbyService;
import com.clicker.clickort.service.impl.LobbyServiceImpl;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyWebSocketHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private LobbyService lobbyService;
    private WebSocketSessionRegistry registry;
    private LobbyWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        InMemoryLobbyRepository repository = new InMemoryLobbyRepository();
        LobbyProperties properties = new LobbyProperties(2, 20);
        lobbyService = new LobbyServiceImpl(repository, properties);
        registry = new WebSocketSessionRegistry(mapper);
        handler = new LobbyWebSocketHandler(lobbyService, registry, mapper);
    }

    private WebSocketSession mockSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    @Test
    void connectionEstablishedSendsInitialLobbyState() throws Exception {
        WebSocketSession session = mockSession("s1");
        handler.afterConnectionEstablished(session);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeastOnce()).sendMessage(captor.capture());

        JsonNode response = mapper.readTree(captor.getValue().getPayload());
        assertEquals("LOBBY_STATE", response.get("type").asText());
        assertEquals(0, response.get("state").get("players").size());
    }

    @Test
    void joinSucceedsAndBindsPlayer() throws Exception {
        WebSocketSession session = mockSession("s1");
        handler.afterConnectionEstablished(session);

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"JOIN\",\"nickname\":\"Alice\"}"));

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeast(2)).sendMessage(captor.capture());

        // One of the messages sent should be JOINED
        boolean hasJoined = captor.getAllValues().stream()
                .anyMatch(msg -> msg.getPayload().contains("\"type\":\"JOINED\"") && msg.getPayload().contains("playerId"));
        assertTrue(hasJoined, "Expected JOINED message sent to session");

        assertTrue(registry.playerId("s1").isPresent());
    }

    @Test
    void invalidJsonReturnsError() throws Exception {
        WebSocketSession session = mockSession("s1");
        handler.afterConnectionEstablished(session);

        handler.handleTextMessage(session, new TextMessage("invalid json {"));

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeast(2)).sendMessage(captor.capture());

        boolean hasError = captor.getAllValues().stream()
                .anyMatch(msg -> msg.getPayload().contains("\"code\":\"INVALID_MESSAGE\""));
        assertTrue(hasError, "Expected INVALID_MESSAGE error response");
    }

    @Test
    void connectionClosedRemovesPlayerAndBroadcasts() {
        WebSocketSession session = mockSession("s1");
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"JOIN\",\"nickname\":\"Alice\"}"));

        assertEquals(1, lobbyService.state().players().size());

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        assertEquals(0, lobbyService.state().players().size());
        assertTrue(registry.playerId("s1").isEmpty());
    }
}
