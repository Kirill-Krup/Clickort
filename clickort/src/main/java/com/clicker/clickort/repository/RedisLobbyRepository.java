package com.clicker.clickort.repository;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis layout:
 * <ul>
 *   <li>{@code lobby:nicknames} hash: lower-cased nickname to playerId (HSETNX gives uniqueness)</li>
 *   <li>{@code lobby:players} hash: playerId to nickname</li>
 *   <li>{@code lobby:ready} set of playerIds</li>
 * </ul>
 */
@Repository
public class RedisLobbyRepository implements LobbyRepository {

    static final String NICKNAMES_KEY = "lobby:nicknames";
    static final String PLAYERS_KEY = "lobby:players";
    static final String READY_KEY = "lobby:ready";

    private final StringRedisTemplate redis;

    public RedisLobbyRepository(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean addPlayer(String playerId, String nickname) {
        Boolean reserved = redis.opsForHash().putIfAbsent(NICKNAMES_KEY, nicknameKey(nickname), playerId);
        if (!Boolean.TRUE.equals(reserved)) {
            return false;
        }
        try {
            redis.opsForHash().put(PLAYERS_KEY, playerId, nickname);
        } catch (Exception e) {
            redis.opsForHash().delete(NICKNAMES_KEY, nicknameKey(nickname));
            throw e;
        }
        return true;
    }

    @Override
    public Optional<String> nicknameOf(String playerId) {
        Object nickname = redis.opsForHash().get(PLAYERS_KEY, playerId);
        return Optional.ofNullable((String) nickname);
    }

    @Override
    public Optional<String> playerIdOf(String nickname) {
        Object playerId = redis.opsForHash().get(NICKNAMES_KEY, nicknameKey(nickname));
        return Optional.ofNullable((String) playerId);
    }

    @Override
    public void removePlayer(String playerId) {
        Optional<String> nickname = nicknameOf(playerId);
        redis.opsForSet().remove(READY_KEY, playerId);
        redis.opsForHash().delete(PLAYERS_KEY, playerId);
        nickname.ifPresent(n -> redis.opsForHash().delete(NICKNAMES_KEY, nicknameKey(n)));
    }

    @Override
    public void markReady(String playerId) {
        redis.opsForSet().add(READY_KEY, playerId);
    }

    @Override
    public void clearReady() {
        redis.delete(READY_KEY);
    }

    @Override
    public Map<String, String> players() {
        Map<String, String> result = new LinkedHashMap<>();
        redis.opsForHash().entries(PLAYERS_KEY)
                .forEach((id, nickname) -> result.put((String) id, (String) nickname));
        return result;
    }

    @Override
    public Set<String> readyPlayers() {
        Set<String> members = redis.opsForSet().members(READY_KEY);
        return members == null ? Set.of() : members;
    }

    private static String nicknameKey(String nickname) {
        return nickname.toLowerCase(Locale.ROOT);
    }
}
