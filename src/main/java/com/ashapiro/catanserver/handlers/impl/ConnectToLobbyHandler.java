package com.ashapiro.catanserver.handlers.impl;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import com.ashapiro.catanserver.socketPayload.lobby.SocketRequestConnectToLobbyPayload;
import com.ashapiro.catanserver.socketPayload.lobby.SocketResponseConnectToLobbyPayload;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.handlers.EventHandler;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.util.LobbyUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConnectToLobbyHandler implements EventHandler {

    private final UserService userService;

    private final Map<Socket, String> socketMap;

    private final ObjectMapper objectMapper;

    private final LobbyUtils lobbyUtils;

    @Transactional
    @Override
    public <T extends SocketMessagePayload> void handle(T message, Socket clientSocket) {
        sendMessageToConnectedUser(clientSocket);
        if (message instanceof SocketRequestConnectToLobbyPayload) {
            SocketRequestConnectToLobbyPayload lobbySocketMessage = (SocketRequestConnectToLobbyPayload) message;
            String token = lobbySocketMessage.getToken();

            User user = userService.findUserByToken(token)
                    .orElseThrow(() -> new NoSuchElementException("User not found with token: " + token));
            Lobby lobby = user.getUserToLobby().getLobby();

            addUserToMap(clientSocket, token);
            updateStatusConnect(user.getUserToLobby());
            List<String> allTokensInLobby = lobby.getAllTokenUsersInLobby();

            lobbyUtils.sendMessageToAllUsersInLobby(
                    allTokensInLobby,
                    new SimpleUserDto(user.getId(), user.getUsername()),
                    EventType.BROADCAST_USER_CONNECTION_TO_LOBBY
            );
        }
    }

    private void sendMessageToConnectedUser(Socket clientSocket) {
        SocketResponseConnectToLobbyPayload message = SocketResponseConnectToLobbyPayload.builder()
                .eventType(EventType.RESPONSE_CONNECT_TO_LOBBY)
                .message("Successfully connected to lobby")
                .build();
        try {
            String response = objectMapper.writeValueAsString(message);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            printWriter.println(response);
            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateStatusConnect(UserToLobby userToLobby) {
        userToLobby.setStatus(UserToLobby.ConnectionStatus.CONNECTED);
    }

    private void addUserToMap(Socket clientSocket, String token) {
        socketMap.put(clientSocket, token);
    }
}
