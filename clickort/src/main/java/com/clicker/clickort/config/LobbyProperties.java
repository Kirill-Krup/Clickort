package com.clicker.clickort.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Lobby settings, bound from the {@code game.lobby.*} properties. */
@ConfigurationProperties(prefix = "game.lobby")
public record LobbyProperties(
        @DefaultValue("5") int minPlayers,
        @DefaultValue("20") int maxNicknameLength) {
}
