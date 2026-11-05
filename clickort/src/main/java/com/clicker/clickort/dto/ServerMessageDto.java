package com.clicker.clickort.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Every message the server sends. Fields that do not apply to a given type are omitted from the JSON.
 *
 * <pre>
 * {"type":"JOINED","playerId":"..."}
 * {"type":"LOBBY_STATE","state":{"players":[{"nickname":"a","ready":false}],"minPlayers":5,"canReady":false,"allReady":false}}
 * {"type":"SCORES","scores":[{"nickname":"a","score":42}]}
 * {"type":"ERROR","code":"NICKNAME_TAKEN","message":"..."}
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServerMessageDto(
        String type,
        String playerId,
        LobbyStateDto state,
        String code,
        String message,
        List<PlayerScoreDto> scores
) {

    public static ServerMessageDto joined(String playerId) {
        return new ServerMessageDto("JOINED", playerId, null, null, null, null);
    }

    public static ServerMessageDto lobbyState(LobbyStateDto state) {
        return new ServerMessageDto("LOBBY_STATE", null, state, null, null, null);
    }

    public static ServerMessageDto scores(List<PlayerScoreDto> scores) {
        return new ServerMessageDto("SCORES", null, null, null, null, scores);
    }

    public static ServerMessageDto error(String code, String message) {
        return new ServerMessageDto("ERROR", null, null, code, message, null);
    }
}
