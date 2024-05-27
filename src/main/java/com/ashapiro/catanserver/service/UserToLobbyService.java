package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.entity.UserToLobby;

import java.util.List;

public interface UserToLobbyService {

    UserToLobby createSession(User user, Lobby lobby);

    void deleteByUserId(Long userId);

    void removeUserFromLobby(User user);

}
