package com.clicker.clickort.service.impl;

import com.clicker.clickort.dto.PlayerScoreDto;
import com.clicker.clickort.dto.ServerMessageDto;
import com.clicker.clickort.service.GameService;
import com.clicker.clickort.service.GameTickerService;
import com.clicker.clickort.websocket.WebSocketSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameTickerServiceImpl implements GameTickerService {

    private static final Logger log = LoggerFactory.getLogger(GameTickerServiceImpl.class);

    private final GameService gameService;
    private final WebSocketSessionRegistry registry;

    public GameTickerServiceImpl(GameService gameService, WebSocketSessionRegistry registry) {
        this.gameService = gameService;
        this.registry = registry;
    }

    @Override
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
