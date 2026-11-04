package com.clicker.clickort.websocket;

import com.clicker.clickort.config.LobbyProperties;
import com.clicker.clickort.repository.InMemoryLobbyRepository;
import com.clicker.clickort.service.GameService;
import com.clicker.clickort.service.LobbyService;
import com.clicker.clickort.service.impl.KafkaMessengerServiceImpl;
import com.clicker.clickort.service.impl.LobbyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DisplayName("LobbyWebSocketHandler Unit Tests")
class LobbyWebSocketHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private LobbyService lobbyService;
    private GameService gameService;
    private WebSocketSessionRegistry registry;
    private LobbyWebSocketHandler handler;
    private KafkaMessengerServiceImpl kafkaMessengerService;

    @BeforeEach
    void setUp() {
        InMemoryLobbyRepository repository = new InMemoryLobbyRepository();
        LobbyProperties properties = new LobbyProperties(2, 20);
        lobbyService = new LobbyServiceImpl(repository, properties);
        gameService = mock(GameService.class);
        registry = new WebSocketSessionRegistry(mapper);
        handler = new LobbyWebSocketHandler(lobbyService, registry, mapper, gameService, kafkaMessengerService);
    }

    private WebSocketSession mockSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    @Test
    @DisplayName("afterConnectionEstablished sends current LOBBY_STATE to the connecting client")
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
    @DisplayName("handleTextMessage with JOIN succeeds, registers session, and returns JOINED message")
    void joinSucceedsAndBindsPlayer() throws Exception {
        WebSocketSession session = mockSession("s1");
        handler.afterConnectionEstablished(session);

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"JOIN\",\"nickname\":\"Alice\"}"));

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeast(2)).sendMessage(captor.capture());

        boolean hasJoined = captor.getAllValues().stream()
                .anyMatch(msg -> msg.getPayload().contains("\"type\":\"JOINED\"") && msg.getPayload().contains("playerId"));
        assertTrue(hasJoined, "Expected JOINED message sent to session");

        assertTrue(registry.playerId("s1").isPresent());
    }

    @Test
    @DisplayName("handleTextMessage with CLICK delegates execution to GameService")
    void clickDelegatesToGameService() {
        WebSocketSession session = mockSession("s1");
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"JOIN\",\"nickname\":\"Alice\"}"));

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"CLICK\"}"));
        verify(gameService, atLeastOnce()).registerClick(anyString());
    }

    @Test
    @DisplayName("handleTextMessage with invalid JSON returns INVALID_MESSAGE error response")
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
    @DisplayName("afterConnectionClosed cleans up player from lobby and leaderboard, then broadcasts state")
    void connectionClosedRemovesPlayerAndBroadcasts() {
        WebSocketSession session = mockSession("s1");
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"JOIN\",\"nickname\":\"Alice\"}"));

        assertEquals(1, lobbyService.state().players().size());

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        assertEquals(0, lobbyService.state().players().size());
        assertTrue(registry.playerId("s1").isEmpty());
        verify(gameService, atLeastOnce()).removePlayer(anyString());
    }
}
