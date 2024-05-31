package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.entity.UserToLobby;

public interface UserToLobbyService {

    UserToLobby createSession(UserEntity userEntity, LobbyEntity lobbyEntity);

    void deleteByUserId(Long userId);

    void removeUserFromLobby(UserEntity userEntity);

}
