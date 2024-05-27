package com.ashapiro.catanserver.handlers.impl;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.handlers.EventHandler;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.util.LobbyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DisconnectFromLobbyHandler implements EventHandler {

    private final UserService userService;

    private final LobbyService lobbyService;

    private final LobbyUtils lobbyUtils;

    private final Map<Socket, String> socketMap;

    @Transactional
    @Override
    public <T extends SocketMessagePayload> void handle(T message, Socket clientSocket) {
        log.info("Handling disconnect from lobby...");
        String token = socketMap.get(clientSocket);
        if (token != null && !token.isEmpty()) {
            try {
                User user = userService.findUserByToken(token)
                        .orElseThrow(() -> new NoSuchElementException("User not found with token: " + token));

                Lobby lobby = lobbyService.removeUserFromLobby(user)
                        .orElseThrow(() -> new NoSuchElementException("Lobby not found with user: " + user));

                List<String> allTokensInLobby = lobby.getAllTokenUsersInLobby();
                if (!allTokensInLobby.isEmpty()) {
                    lobbyUtils.sendMessageToAllUsersInLobby(
                            allTokensInLobby,
                            new SimpleUserDto(user.getId(), user.getUsername()),
                            EventType.BROADCAST_USER_DISCONNECT_FROM_LOBBY);
                }
            } catch (NoSuchElementException e) {
                log.error("Failed to remove user from lobby: " + e.getMessage());
            }
        }
    }
}
