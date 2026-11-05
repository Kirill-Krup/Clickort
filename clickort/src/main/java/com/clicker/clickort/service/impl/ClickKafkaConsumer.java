package com.clicker.clickort.service.impl;

import com.clicker.clickort.dto.ClickEvent;
import com.clicker.clickort.service.LobbyLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickKafkaConsumer {

    private final LobbyLeaderboardService lobbyLeaderboardService;

    @KafkaListener(topics = "${topic.name}", groupId = "${spring.kafka.consumer.group-id:clicker-consumer-group}")
    public void handleClickEvent(ClickEvent clickEvent) {
        if (clickEvent != null && clickEvent.nickname() != null && !clickEvent.nickname().isBlank()) {
            String lobbyId = clickEvent.lobbyId() != null && !clickEvent.lobbyId().isBlank()
                    ? clickEvent.lobbyId()
                    : GameServiceImpl.DEFAULT_LOBBY_ID;
            lobbyLeaderboardService.registerClick(lobbyId, clickEvent.nickname());
        }
    }
}
