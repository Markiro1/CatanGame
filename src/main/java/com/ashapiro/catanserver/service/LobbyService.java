package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyRequestDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyResponseDto;
import com.ashapiro.catanserver.dto.lobby.LobbyDetailsDto;
import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.entity.User;

import java.util.List;
import java.util.Optional;

public interface LobbyService {

    CreateLobbyResponseDto createLobby(CreateLobbyRequestDto request);

    List<AllLobbyDto> getAllLobbies();

    void joinToLobby(Long lobbyId, String token);

    Optional<Lobby> removeUserFromLobby(User user);

    LobbyDetailsDto extractLobbyDetails(String token);

}
