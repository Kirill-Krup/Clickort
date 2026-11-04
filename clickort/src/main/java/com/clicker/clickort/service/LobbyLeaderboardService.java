package com.clicker.clickort.service;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

public interface LobbyLeaderboardService {
    void registerClick(String lobbyId, String nickname);

    Set<ZSetOperations.TypedTuple<String>> getLobbyLeaderboard(String lobbyId);
}
