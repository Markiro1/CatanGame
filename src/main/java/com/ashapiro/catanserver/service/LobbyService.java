package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDTO;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyRequestDTO;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyResponseDTO;
import com.ashapiro.catanserver.dto.lobby.LobbyDataDTO;
import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface LobbyService {

    CreateLobbyResponseDTO createLobby(CreateLobbyRequestDTO request);

    List<AllLobbyDTO> getAllLobbies();

    void joinToLobby(Long lobbyId, String token);

    Optional<LobbyEntity> removeUserEntityFromLobby(UserEntity userEntity);

    LobbyDataDTO extractLobbyDetails(String token);

}
