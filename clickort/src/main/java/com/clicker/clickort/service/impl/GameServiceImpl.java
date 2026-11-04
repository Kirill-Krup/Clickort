package com.clicker.clickort.service.impl;

import com.clicker.clickort.service.GameService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Service
public class GameServiceImpl implements GameService {

    private static final String GAME_SCORES_KEY = "game:scores";
    private final StringRedisTemplate redis;
    private final ExecutorService gameExecutor;

    public GameServiceImpl(StringRedisTemplate redis, ExecutorService gameExecutor) {
        this.redis = redis;
        this.gameExecutor = gameExecutor;
    }

    @Override
    public void registerClick(String playerId) {
        gameExecutor.submit(() -> {
            redis.opsForHash().increment(GAME_SCORES_KEY, playerId, 1);
        });
    }

    @Override
    public long getScore(String playerId) {
        Object score = redis.opsForHash().get(GAME_SCORES_KEY, playerId);
        return score != null ? Long.parseLong(String.valueOf(score)) : 0L;
    }

    @Override
    public Map<String, Long> getAllScores(String playerId) {
        Map<Object, Object> raw = redis.opsForHash().entries(GAME_SCORES_KEY);
        Map<String, Long> result = new HashMap<>();
        raw.forEach((k, v) -> {result.put((String) k, Long.parseLong(String.valueOf(v)));});
        return result;
    }

    @Override
    public void resetScores() {
        redis.delete(GAME_SCORES_KEY);
    }


}
