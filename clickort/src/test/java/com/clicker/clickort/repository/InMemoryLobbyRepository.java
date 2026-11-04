package com.clicker.clickort.repository;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link LobbyRepository} for testing without Redis.
 */
public class InMemoryLobbyRepository implements LobbyRepository {

    private final Map<String, String> players = new ConcurrentHashMap<>();
    private final Map<String, String> nicknames = new ConcurrentHashMap<>();
    private final Set<String> ready = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized boolean addPlayer(String playerId, String nickname) {
        String key = nickname.toLowerCase(Locale.ROOT);
        if (nicknames.containsKey(key)) {
            return false;
        }
        nicknames.put(key, playerId);
        players.put(playerId, nickname);
        return true;
    }

    @Override
    public Optional<String> nicknameOf(String playerId) {
        return Optional.ofNullable(players.get(playerId));
    }

    @Override
    public synchronized void removePlayer(String playerId) {
        String nickname = players.remove(playerId);
        ready.remove(playerId);
        if (nickname != null) {
            nicknames.remove(nickname.toLowerCase(Locale.ROOT));
        }
    }

    @Override
    public void markReady(String playerId) {
        ready.add(playerId);
    }

    @Override
    public Map<String, String> players() {
        return Map.copyOf(players);
    }

    @Override
    public Set<String> readyPlayers() {
        return Set.copyOf(ready);
    }

    public synchronized void clear() {
        players.clear();
        nicknames.clear();
        ready.clear();
    }
}
