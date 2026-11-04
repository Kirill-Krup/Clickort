package com.clicker.clickort.service;

import com.clicker.clickort.dto.PlayerScoreDto;

import java.util.List;

public interface GameService {
    void registerClick(String playerId);

    long getScore(String playerId);

    List<PlayerScoreDto> getLeaderboard();

    void resetScores();

    void removePlayer(String playerId);
}
