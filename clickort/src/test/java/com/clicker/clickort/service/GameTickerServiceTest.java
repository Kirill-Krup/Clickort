package com.clicker.clickort.service;

import com.clicker.clickort.dto.PlayerScoreDto;
import com.clicker.clickort.dto.ServerMessageDto;
import com.clicker.clickort.service.impl.GameTickerServiceImpl;
import com.clicker.clickort.websocket.WebSocketSessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GameTickerService Unit Tests")
class GameTickerServiceTest {

    private GameService gameService;
    private WebSocketSessionRegistry registry;
    private GameTickerServiceImpl tickerService;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        registry = mock(WebSocketSessionRegistry.class);
        tickerService = new GameTickerServiceImpl(gameService, registry);
    }

    @Test
    @DisplayName("broadcastLeaderboard broadcasts SCORES when leaderboard contains scores")
    void broadcastsWhenLeaderboardNotEmpty() {
        List<PlayerScoreDto> scores = List.of(new PlayerScoreDto("Alice", 50L), new PlayerScoreDto("Bob", 20L));
        when(gameService.getLeaderboard()).thenReturn(scores);

        tickerService.broadcastLeaderboard();

        ArgumentCaptor<ServerMessageDto> captor = ArgumentCaptor.forClass(ServerMessageDto.class);
        verify(registry).broadcast(captor.capture());

        ServerMessageDto message = captor.getValue();
        assertEquals("SCORES", message.type());
        assertEquals(2, message.scores().size());
        assertEquals("Alice", message.scores().get(0).nickname());
    }

    @Test
    @DisplayName("broadcastLeaderboard does nothing when leaderboard is empty")
    void doesNotBroadcastWhenEmpty() {
        when(gameService.getLeaderboard()).thenReturn(List.of());

        tickerService.broadcastLeaderboard();
        verify(registry, never()).broadcast(any());
    }

    @Test
    @DisplayName("broadcastLeaderboard gracefully handles exceptions without propagating errors")
    void handlesExceptionsGracefully() {
        when(gameService.getLeaderboard()).thenThrow(new RuntimeException("Redis connection timed out"));

        assertDoesNotThrow(() -> tickerService.broadcastLeaderboard());
        verify(registry, never()).broadcast(any());
    }
}
