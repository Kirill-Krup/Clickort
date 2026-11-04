package com.clicker.clickort.service;

import java.util.Map;

public interface GameService {
    void registerClick(String playerId);
    long getScore(String playerId);

    Map<String, Long> getAllScores(String playerId);

    void resetScores();
}
