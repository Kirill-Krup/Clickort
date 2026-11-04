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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void joinSucceedsWithValidNickname() {
        String playerId = lobbyService.join("Alice");
        assertNotNull(playerId);
        assertEquals("Alice", repository.nicknameOf(playerId).orElseThrow());
    }

    @Test
    void joinFailsWithBlankOrTooLongNickname() {
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join(""));
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join("   "));
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join(null));
        assertThrows(InvalidNicknameException.class, () -> lobbyService.join("verylongnicknamethatexceeds10"));
    }

    @Test
    void joinRejectsCaseInsensitiveDuplicates() {
        lobbyService.join("Alice");
        assertThrows(NicknameTakenException.class, () -> lobbyService.join("alice"));
        assertThrows(NicknameTakenException.class, () -> lobbyService.join("ALICE"));
    }

    @Test
    void readyFailsWhenNotEnoughPlayers() {
        String aliceId = lobbyService.join("Alice");
        assertThrows(NotEnoughPlayersException.class, () -> lobbyService.ready(aliceId));
    }

    @Test
    void readyFailsForUnknownPlayer() {
        assertThrows(PlayerNotFoundException.class, () -> lobbyService.ready("unknown-id"));
    }

    @Test
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
    void leaveRemovesPlayerAndFreesNickname() {
        String p1 = lobbyService.join("Alice");
        assertEquals(1, lobbyService.state().players().size());

        lobbyService.leave(p1);
        assertEquals(0, lobbyService.state().players().size());

        // Can rejoin with Alice
        assertDoesNotThrow(() -> lobbyService.join("Alice"));
    }
}
