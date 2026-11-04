package com.clicker.clickort.service;

import com.clicker.clickort.dto.PlayerScoreDto;
import com.clicker.clickort.dto.ServerMessageDto;
import com.clicker.clickort.websocket.WebSocketSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Periodically broadcasts current leaderboard scores to all connected clients.
 * This decouples score broadcasting from the high-frequency incoming click stream.
 */
@Service
public class GameTickerService {

    private static final Logger log = LoggerFactory.getLogger(GameTickerService.class);

    private final GameService gameService;
    private final WebSocketSessionRegistry registry;

    public GameTickerService(GameService gameService, WebSocketSessionRegistry registry) {
        this.gameService = gameService;
        this.registry = registry;
    }

    /**
     * Broadcasts scores 10 times a second (every 100 ms) if there are active scores.
     */
    @Scheduled(fixedRate = 100)
    public void broadcastLeaderboard() {
        try {
            List<PlayerScoreDto> leaderboard = gameService.getLeaderboard();
            if (leaderboard != null && !leaderboard.isEmpty()) {
                registry.broadcast(ServerMessageDto.scores(leaderboard));
            }
        } catch (Exception e) {
            log.trace("Error broadcasting leaderboard scores: {}", e.getMessage());
        }
    }
}
