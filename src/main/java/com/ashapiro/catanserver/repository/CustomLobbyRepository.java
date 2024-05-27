package com.ashapiro.catanserver.repository;

import com.ashapiro.catanserver.dto.lobby.LobbyDetailsDto;

public interface CustomLobbyRepository {

    LobbyDetailsDto getLobbyDetailsByUserToken(String token);
}
