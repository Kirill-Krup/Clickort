package com.clicker.clickort.exception;

/**
 * Base class for expected lobby errors. Every error has a stable {@link #code()}
 * that is sent to the client, so the frontend can react without parsing messages.
 */
public abstract class LobbyException extends RuntimeException {

    private final String code;

    protected LobbyException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
