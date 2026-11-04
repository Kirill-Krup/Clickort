package com.clicker.clickort.exception;


/** A connection that already joined the lobby tried to JOIN again. */
public final class AlreadyJoinedException extends LobbyException {

    public AlreadyJoinedException() {
        super("ALREADY_JOINED", "This connection has already joined the lobby");
    }
}
