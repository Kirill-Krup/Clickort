package com.clicker.clickort.dto;

import java.time.Instant;

public record ClickEvent(String lobbyId, String playerId, String nickname, Instant timestamp) {
}
