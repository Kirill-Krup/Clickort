package com.clicker.clickort.service.impl;

import com.clicker.clickort.dto.ClickEvent;
import com.clicker.clickort.service.LobbyLeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@DisplayName("ClickKafkaConsumer Unit Tests")
class ClickKafkaConsumerTest {

    private LobbyLeaderboardService leaderboardService;
    private ClickKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        leaderboardService = mock(LobbyLeaderboardService.class);
        consumer = new ClickKafkaConsumer(leaderboardService);
    }

    @Test
    @DisplayName("handleClickEvent updates leaderboard when event is valid")
    void updatesLeaderboardOnValidEvent() {
        ClickEvent event = new ClickEvent("main", "Alice");
        consumer.handleClickEvent(event);

        verify(leaderboardService).registerClick("main", "Alice");
    }

    @Test
    @DisplayName("handleClickEvent uses default lobby if event lobbyId is empty or null")
    void fallbackToDefaultLobbyIfEmpty() {
        ClickEvent event = new ClickEvent("", "Bob");
        consumer.handleClickEvent(event);

        verify(leaderboardService).registerClick(GameServiceImpl.DEFAULT_LOBBY_ID, "Bob");
    }

    @Test
    @DisplayName("handleClickEvent ignores null event or blank nickname")
    void ignoresInvalidEvent() {
        consumer.handleClickEvent(null);
        consumer.handleClickEvent(new ClickEvent("main", ""));
        consumer.handleClickEvent(new ClickEvent("main", "   "));
        consumer.handleClickEvent(new ClickEvent("main", null));

        verify(leaderboardService, never()).registerClick(anyString(), anyString());
    }
}
