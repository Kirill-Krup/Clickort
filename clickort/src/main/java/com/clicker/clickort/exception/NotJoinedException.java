package com.clicker.clickort.exception;

/** A connection that has not joined the lobby tried to do something that requires it. */
public final class NotJoinedException extends LobbyException {

    public NotJoinedException() {
        super("NOT_JOINED", "Join the lobby first");
    }
}
