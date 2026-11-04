package com.clicker.clickort.service.impl;

import com.clicker.clickort.dto.PlayerScoreDto;
import com.clicker.clickort.service.LobbyLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LobbyLeaderboardServiceImpl implements LobbyLeaderboardService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void registerClick(String lobbyId, String nickname) {
        String leaderboardKey = leaderboardKey(lobbyId);
        redisTemplate.opsForZSet().incrementScore(leaderboardKey, nickname, 1.0);
    }

    @Override
    public List<PlayerScoreDto> getLobbyLeaderboard(String lobbyId) {
        String leaderboardKey = leaderboardKey(lobbyId);
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(leaderboardKey, 0, -1);
        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }
        List<PlayerScoreDto> result = new ArrayList<>(tuples.size());
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String nickname = tuple.getValue();
            Double score = tuple.getScore();
            if (nickname != null && score != null) {
                result.add(new PlayerScoreDto(nickname, score.longValue()));
            }
        }
        return result;
    }

    @Override
    public void resetLobby(String lobbyId) {
        redisTemplate.delete(leaderboardKey(lobbyId));
    }

    @Override
    public void removePlayer(String lobbyId, String nickname) {
        redisTemplate.opsForZSet().remove(leaderboardKey(lobbyId), nickname);
    }

    private static String leaderboardKey(String lobbyId) {
        return "lobby:" + lobbyId + ":leaderboard";
    }
}
