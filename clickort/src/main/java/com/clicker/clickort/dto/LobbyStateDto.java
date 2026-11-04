package com.clicker.clickort.dto;

import java.util.List;

/**
 * Snapshot of the lobby.
 *
 * @param canReady true when there are enough players, so the READY button is shown
 * @param allReady true when there are enough players and every one of them is ready
 */
public record LobbyStateDto(List<PlayerDto> players, int minPlayers, boolean canReady, boolean allReady) {
}
