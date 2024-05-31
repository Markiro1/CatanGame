package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyRequestDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyResponseDto;
import com.ashapiro.catanserver.dto.lobby.LobbyDataDTO;
import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface LobbyService {

    CreateLobbyResponseDto createLobby(CreateLobbyRequestDto request);

    List<AllLobbyDto> getAllLobbies();

    void joinToLobby(Long lobbyId, String token);

    Optional<LobbyEntity> removeUserFromLobby(UserEntity userEntity);

    LobbyDataDTO extractLobbyDetails(String token);

}
