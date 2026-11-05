package com.clicker.clickort.exception;

/** There is no player with this id in the lobby. */
public final class PlayerNotFoundException extends LobbyException {

    public PlayerNotFoundException(String playerId) {
        super("PLAYER_NOT_FOUND", "Unknown player: " + playerId);
    }
}
