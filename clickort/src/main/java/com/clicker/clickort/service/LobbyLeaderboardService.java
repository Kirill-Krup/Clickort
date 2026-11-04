package com.clicker.clickort.service;

import com.clicker.clickort.dto.PlayerScoreDto;

import java.util.List;

public interface LobbyLeaderboardService {
    void registerClick(String lobbyId, String nickname);

    List<PlayerScoreDto> getLobbyLeaderboard(String lobbyId);

    void resetLobby(String lobbyId);

    void removePlayer(String lobbyId, String nickname);
}
