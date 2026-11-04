package com.clicker.clickort.service.impl;

import com.clicker.clickort.dto.PlayerScoreDto;
import com.clicker.clickort.repository.LobbyRepository;
import com.clicker.clickort.service.GameService;
import com.clicker.clickort.service.LobbyLeaderboardService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class GameServiceImpl implements GameService {

    public static final String DEFAULT_LOBBY_ID = "main";

    private final LobbyLeaderboardService leaderboardService;
    private final LobbyRepository lobbyRepository;
    private final ExecutorService gameExecutor;

    public GameServiceImpl(LobbyLeaderboardService leaderboardService,
                           LobbyRepository lobbyRepository,
                           ExecutorService gameExecutor) {
        this.leaderboardService = leaderboardService;
        this.lobbyRepository = lobbyRepository;
        this.gameExecutor = gameExecutor;
    }

    @Override
    public void registerClick(String playerId) {
        gameExecutor.submit(() -> {
            lobbyRepository.nicknameOf(playerId).ifPresent(nickname ->
                    leaderboardService.registerClick(DEFAULT_LOBBY_ID, nickname)
            );
        });
    }

    @Override
    public long getScore(String playerId) {
        return lobbyRepository.nicknameOf(playerId)
                .map(nickname -> {
                    List<PlayerScoreDto> leaderboard = leaderboardService.getLobbyLeaderboard(DEFAULT_LOBBY_ID);
                    return leaderboard.stream()
                            .filter(p -> p.nickname().equalsIgnoreCase(nickname))
                            .mapToLong(PlayerScoreDto::score)
                            .findFirst()
                            .orElse(0L);
                })
                .orElse(0L);
    }

    @Override
    public List<PlayerScoreDto> getLeaderboard() {
        return leaderboardService.getLobbyLeaderboard(DEFAULT_LOBBY_ID);
    }

    @Override
    public void resetScores() {
        leaderboardService.resetLobby(DEFAULT_LOBBY_ID);
    }

    @Override
    public void removePlayer(String playerId) {
        lobbyRepository.nicknameOf(playerId)
                .ifPresent(nickname -> leaderboardService.removePlayer(DEFAULT_LOBBY_ID, nickname));
    }
}
