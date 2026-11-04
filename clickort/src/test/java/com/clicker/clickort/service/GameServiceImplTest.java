package com.clicker.clickort.service;

import com.clicker.clickort.dto.PlayerScoreDto;
import com.clicker.clickort.repository.LobbyRepository;
import com.clicker.clickort.service.impl.GameServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GameServiceImpl Unit Tests")
class GameServiceImplTest {

    private LobbyLeaderboardService leaderboardService;
    private LobbyRepository lobbyRepository;
    private ExecutorService executor;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        leaderboardService = mock(LobbyLeaderboardService.class);
        lobbyRepository = mock(LobbyRepository.class);
        executor = Executors.newSingleThreadExecutor();
        gameService = new GameServiceImpl(leaderboardService, lobbyRepository, executor);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("registerClick resolves nickname from repository and submits click to leaderboardService")
    void registerClickSucceedsForKnownPlayer() throws InterruptedException {
        String playerId = "p1";
        when(lobbyRepository.nicknameOf(playerId)).thenReturn(Optional.of("Alice"));

        gameService.registerClick(playerId);

        // Wait briefly for executor task to finish
        Thread.sleep(100);

        verify(lobbyRepository).nicknameOf(playerId);
        verify(leaderboardService).registerClick(GameServiceImpl.DEFAULT_LOBBY_ID, "Alice");
    }

    @Test
    @DisplayName("registerClick does not submit click if playerId is unknown")
    void registerClickIgnoredForUnknownPlayer() throws InterruptedException {
        String playerId = "unknown";
        when(lobbyRepository.nicknameOf(playerId)).thenReturn(Optional.empty());

        gameService.registerClick(playerId);
        Thread.sleep(100);

        verify(leaderboardService, never()).registerClick(anyString(), anyString());
    }

    @Test
    @DisplayName("getScore returns the player's score from the leaderboard")
    void getScoreReturnsCorrectValue() {
        String playerId = "p1";
        when(lobbyRepository.nicknameOf(playerId)).thenReturn(Optional.of("Alice"));
        when(leaderboardService.getLobbyLeaderboard(GameServiceImpl.DEFAULT_LOBBY_ID))
                .thenReturn(List.of(new PlayerScoreDto("Alice", 42L), new PlayerScoreDto("Bob", 15L)));

        long score = gameService.getScore(playerId);
        assertEquals(42L, score);
    }

    @Test
    @DisplayName("getScore returns 0 if player has no recorded score or is unknown")
    void getScoreReturnsZeroForMissingScore() {
        when(lobbyRepository.nicknameOf("unknown")).thenReturn(Optional.empty());
        assertEquals(0L, gameService.getScore("unknown"));

        when(lobbyRepository.nicknameOf("p2")).thenReturn(Optional.of("Charlie"));
        when(leaderboardService.getLobbyLeaderboard(GameServiceImpl.DEFAULT_LOBBY_ID)).thenReturn(List.of());
        assertEquals(0L, gameService.getScore("p2"));
    }

    @Test
    @DisplayName("getLeaderboard delegates to leaderboardService")
    void getLeaderboardDelegates() {
        List<PlayerScoreDto> mockList = List.of(new PlayerScoreDto("Alice", 100L));
        when(leaderboardService.getLobbyLeaderboard(GameServiceImpl.DEFAULT_LOBBY_ID)).thenReturn(mockList);

        List<PlayerScoreDto> result = gameService.getLeaderboard();
        assertEquals(mockList, result);
    }

    @Test
    @DisplayName("resetScores clears leaderboard in leaderboardService")
    void resetScoresDelegates() {
        gameService.resetScores();
        verify(leaderboardService).resetLobby(GameServiceImpl.DEFAULT_LOBBY_ID);
    }

    @Test
    @DisplayName("removePlayer deletes player score from leaderboard when player exists")
    void removePlayerDelegates() {
        when(lobbyRepository.nicknameOf("p1")).thenReturn(Optional.of("Alice"));

        gameService.removePlayer("p1");
        verify(leaderboardService).removePlayer(GameServiceImpl.DEFAULT_LOBBY_ID, "Alice");
    }
}
