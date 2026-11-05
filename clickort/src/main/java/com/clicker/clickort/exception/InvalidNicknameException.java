package com.clicker.clickort.exception;

/** The nickname is null, blank or too long. */
public final class InvalidNicknameException extends LobbyException {

    public InvalidNicknameException(String message) {
        super("INVALID_NICKNAME", message);
    }
}
