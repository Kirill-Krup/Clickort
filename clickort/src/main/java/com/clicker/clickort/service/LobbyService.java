package com.clicker.clickort.service;

import com.clicker.clickort.dto.LobbyStateDto;

public interface LobbyService {
    String join(String rawNickname);
    void ready(String playerId);
    void leave(String playerId);
    LobbyStateDto state();
}
