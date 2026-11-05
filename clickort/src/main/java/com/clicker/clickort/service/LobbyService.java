package com.clicker.clickort.service;

import com.clicker.clickort.dto.LobbyStateDto;

import java.util.Optional;

public interface LobbyService {
    String join(String rawNickname);
    void ready(String playerId);
    void clearReady();
    void leave(String playerId);
    LobbyStateDto state();
    Optional<String> nicknameOf(String playerId);
    Optional<String> playerIdOf(String nickname);
}
