package com.clicker.clickort.dto;

/** What clients are allowed to see about a player (never the playerId). */
public record PlayerDto(String nickname, boolean ready) {
}
