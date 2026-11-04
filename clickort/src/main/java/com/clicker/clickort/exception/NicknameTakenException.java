package com.clicker.clickort.exception;


/** Another player in the lobby already uses this nickname (compared ignoring case). */
public final class NicknameTakenException extends LobbyException {

    public NicknameTakenException(String nickname) {
        super("NICKNAME_TAKEN", "Nickname is already taken: " + nickname);
    }
}
