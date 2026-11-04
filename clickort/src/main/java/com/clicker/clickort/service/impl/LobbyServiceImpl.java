package com.clicker.clickort.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.clicker.clickort.config.LobbyProperties;
import com.clicker.clickort.dto.LobbyStateDto;
import com.clicker.clickort.dto.PlayerDto;
import com.clicker.clickort.exception.InvalidNicknameException;
import com.clicker.clickort.exception.NicknameTakenException;
import com.clicker.clickort.exception.NotEnoughPlayersException;
import com.clicker.clickort.exception.PlayerNotFoundException;
import com.clicker.clickort.repository.LobbyRepository;
import com.clicker.clickort.service.LobbyService;
import org.springframework.stereotype.Service;

/** Lobby rules: who may join, who may press READY, and what the lobby looks like. */
@Service
public class LobbyServiceImpl implements LobbyService {

    private final LobbyRepository repository;
    private final LobbyProperties properties;

    public LobbyServiceImpl(LobbyRepository repository, LobbyProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * Adds a player to the lobby.
     *
     * @return the generated playerId
     * @throws InvalidNicknameException if the nickname is null, blank or too long
     * @throws NicknameTakenException   if another player already uses it (case-insensitive)
     */
    @Override
    public String join(String rawNickname) {
        String nickname = validate(rawNickname);
        String playerId = UUID.randomUUID().toString();
        if (!repository.addPlayer(playerId, nickname)) {
            throw new NicknameTakenException(nickname);
        }
        return playerId;
    }

    /**
     * Marks the player as ready. Calling it again is harmless.
     *
     * @throws PlayerNotFoundException  if the player is not in the lobby
     * @throws NotEnoughPlayersException if there are fewer than the minimum number of players
     */
    @Override
    public void ready(String playerId) {
        if (repository.nicknameOf(playerId).isEmpty()) {
            throw new PlayerNotFoundException(playerId);
        }
        if (repository.players().size() < properties.minPlayers()) {
            throw new NotEnoughPlayersException(properties.minPlayers());
        }
        repository.markReady(playerId);
    }

    /** Removes the player from the lobby. Unknown ids are ignored. */
    @Override
    public void leave(String playerId) {
        repository.removePlayer(playerId);
    }

    @Override
    public LobbyStateDto state() {
        Map<String, String> players = repository.players();
        Set<String> ready = repository.readyPlayers();

        List<PlayerDto> views = players.entrySet().stream()
                .map(e -> new PlayerDto(e.getValue(), ready.contains(e.getKey())))
                .sorted(Comparator.comparing(PlayerDto::nickname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        boolean canReady = views.size() >= properties.minPlayers();
        boolean allReady = canReady && views.stream().allMatch(PlayerDto::ready);
        return new LobbyStateDto(views, properties.minPlayers(), canReady, allReady);
    }

    private String validate(String rawNickname) {
        if (rawNickname == null || rawNickname.isBlank()) {
            throw new InvalidNicknameException("Nickname must not be empty");
        }
        String nickname = rawNickname.trim();
        if (nickname.length() > properties.maxNicknameLength()) {
            throw new InvalidNicknameException(
                    "Nickname must be at most " + properties.maxNicknameLength() + " characters");
        }
        return nickname;
    }
}
