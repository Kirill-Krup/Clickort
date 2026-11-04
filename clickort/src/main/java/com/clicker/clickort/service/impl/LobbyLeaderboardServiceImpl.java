package com.clicker.clickort.service.impl;

import com.clicker.clickort.service.LobbyLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class LobbyLeaderboardServiceImpl implements LobbyLeaderboardService {

    private final StringRedisTemplate redisTemplate;

    public void registerClick(String lobbyId, String nickname) {
        String leaderboardKey = "lobby:" + lobbyId + ":leaderboard";
        redisTemplate.opsForZSet().incrementScore(leaderboardKey, nickname, 1.0);
    }

    public Set<ZSetOperations.TypedTuple<String>> getLobbyLeaderboard(String lobbyId) {
        String leaderboardKey = "lobby:" + lobbyId + ":leaderboard";
        return redisTemplate.opsForZSet().reverseRangeWithScores(leaderboardKey, 0, -1);
    }
}