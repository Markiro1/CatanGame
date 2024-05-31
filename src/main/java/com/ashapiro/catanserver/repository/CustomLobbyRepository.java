package com.ashapiro.catanserver.repository;

import com.ashapiro.catanserver.dto.lobby.LobbyDataDTO;

public interface CustomLobbyRepository {

    LobbyDataDTO getLobbyDetailsByUserToken(String token);
}
