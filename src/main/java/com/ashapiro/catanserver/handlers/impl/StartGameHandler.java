package com.ashapiro.catanserver.handlers.impl;

import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.handlers.EventHandler;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import com.ashapiro.catanserver.socketPayload.game.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.util.LobbyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class StartGameHandler implements EventHandler {

    private final Map<Socket, String> socketMap;

    private final LobbyUtils lobbyUtils;

    private final UserService userService;

    @Override
    public <T extends SocketMessagePayload> void handle(T message, Socket clientSocket) {
        if (message instanceof SocketRequestStartGamePayload) {
            SocketRequestStartGamePayload startGamePayload = (SocketRequestStartGamePayload) message;
            String token = socketMap.get(clientSocket);
            if (token != null && !token.isEmpty()) {
                User user = userService.findUserByToken(token)
                        .orElseThrow(() -> new NoSuchElementException("User not found with token: " + token));
                Lobby lobby = user.getUserToLobby().getLobby();
                List<String> allTokensInLobby = lobby.getAllTokenUsersInLobby();
                lobbyUtils.sendMessageToAllUsersInLobby(allTokensInLobby, null, EventType.BROADCAST_START_GAME);
            }
        }
    }
}
