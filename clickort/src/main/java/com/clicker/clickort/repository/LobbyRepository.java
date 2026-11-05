package com.clicker.clickort.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Storage for lobby data. The production implementation is Redis, so the lobby
 * survives a restart of the backend; tests use an in-memory fake.
 *
 * <p>Nicknames are unique case-insensitively ("Alice" and "alice" are the same nickname).
 */
public interface LobbyRepository {

    /**
     * Atomically adds a player.
     *
     * @return false if the nickname is already taken (nothing is stored in that case)
     */
    boolean addPlayer(String playerId, String nickname);

    Optional<String> nicknameOf(String playerId);

    Optional<String> playerIdOf(String nickname);

    /** Removes the player, frees the nickname and clears the ready flag. Unknown ids are ignored. */
    void removePlayer(String playerId);

    void markReady(String playerId);

    void clearReady();

    /** All players as playerId to nickname. */
    Map<String, String> players();

    /** Ids of players who pressed READY. */
    Set<String> readyPlayers();
}
