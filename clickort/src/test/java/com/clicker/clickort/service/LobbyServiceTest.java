package com.clicker.clickort.service;

import com.clicker.clickort.config.LobbyProperties;
import com.clicker.clickort.dto.LobbyStateDto;
import com.clicker.clickort.exception.InvalidNicknameException;
import com.clicker.clickort.exception.NicknameTakenException;
import com.clicker.clickort.exception.NotEnoughPlayersException;
import com.clicker.clickort.exception.PlayerNotFoundException;
import com.clicker.clickort.repository.InMemoryLobbyRepository;
import com.clicker.clickort.service.impl.LobbyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LobbyService Unit Tests")
class LobbyServiceTest {

    private InMemoryLobbyRepository repository;
    private LobbyService lobbyService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLobbyRepository();
        LobbyProperties properties = new LobbyProperties(3, 10);
        lobbyService = new LobbyServiceImpl(repository, properties);
    }

    @Test
    @DisplayName("Join succeeds with a valid nickname and stores player")
    void joinSucceedsWithValidNickname() {
        String playerId = lobbyService.join("Alice");
        assertNotNull(playerId);
        assertEquals("Alice", repository.nicknameOf(playerId).orElseThrow());
    }

    @Test
    @DisplayName("Join fails with blank, empty, null or overly long nicknames")
    void joinFailsWithBlankOrTooLongNickname() {
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join(""));
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join("   "));
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join(null));
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join("verylongnicknamethatexceeds10"));
    }

    @Test
    @DisplayName("Join rejects case-insensitive duplicate nicknames")
    void joinRejectsCaseInsensitiveDuplicates() {
        lobbyService.join("Alice");
        assertThrows(NicknameTakenException.class, () -> lobbyService.join("alice"));
        assertThrows(NicknameTakenException.class, () -> lobbyService.join("ALICE"));
    }

    @Test
    @DisplayName("Ready fails when lobby has fewer than required minimum players")
    void readyFailsWhenNotEnoughPlayers() {
        String aliceId = lobbyService.join("Alice");
        assertThrows(NotEnoughPlayersException.class, () -> lobbyService.ready(aliceId));
    }

    @Test
    @DisplayName("Ready fails for unknown or nonexistent playerId")
    void readyFailsForUnknownPlayer() {
        assertThrows(PlayerNotFoundException.class, () -> lobbyService.ready("unknown-id"));
    }

    @Test
    @DisplayName("Ready flag toggles and allReady becomes true only when all players are ready")
    void readyAndAllReadyWorkflow() {
        String p1 = lobbyService.join("Alice");
        String p2 = lobbyService.join("Bob");
        String p3 = lobbyService.join("Charlie");

        LobbyStateDto state = lobbyService.state();
        assertEquals(3, state.players().size());
        assertTrue(state.canReady());
        assertFalse(state.allReady());

        lobbyService.ready(p1);
        lobbyService.ready(p2);
        assertFalse(lobbyService.state().allReady());

        lobbyService.ready(p3);
        assertTrue(lobbyService.state().allReady());
    }

    @Test
    @DisplayName("Leave removes player and frees nickname for reuse")
    void leaveRemovesPlayerAndFreesNickname() {
        String p1 = lobbyService.join("Alice");
        assertEquals(1, lobbyService.state().players().size());

        lobbyService.leave(p1);
        assertEquals(0, lobbyService.state().players().size());

        // Can rejoin with Alice
        assertDoesNotThrow(() -> lobbyService.join("Alice"));
    }
}
