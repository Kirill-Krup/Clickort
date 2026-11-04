package com.clicker.clickort.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Every message the server sends. Fields that do not apply to a given type are omitted from the JSON.
 *
 * <pre>
 * {"type":"JOINED","playerId":"..."}
 * {"type":"LOBBY_STATE","state":{"players":[{"nickname":"a","ready":false}],"minPlayers":5,"canReady":false,"allReady":false}}
 * {"type":"ERROR","code":"NICKNAME_TAKEN","message":"..."}
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServerMessageDto(String type, String playerId, LobbyStateDto state, String code, String message) {

    public static ServerMessageDto joined(String playerId) {
        return new ServerMessageDto("JOINED", playerId, null, null, null);
    }

    public static ServerMessageDto lobbyState(LobbyStateDto state) {
        return new ServerMessageDto("LOBBY_STATE", null, state, null, null);
    }

    public static ServerMessageDto error(String code, String message) {
        return new ServerMessageDto("ERROR", null, null, code, message);
    }
}
