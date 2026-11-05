package com.clicker.clickort.exception;

/** READY was pressed while the lobby has fewer players than the required minimum. */
public final class NotEnoughPlayersException extends LobbyException {

    public NotEnoughPlayersException(int required) {
        super("NOT_ENOUGH_PLAYERS", "At least " + required + " players are required to get ready");
    }
}
