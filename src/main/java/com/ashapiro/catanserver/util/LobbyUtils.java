package com.ashapiro.catanserver.util;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.Lobby;
import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.socketPayload.lobby.SocketBroadcastConnectToLobbyPayload;
import com.ashapiro.catanserver.socketPayload.lobby.SocketBroadcastDisconnectFromLobbyPayload;
import com.ashapiro.catanserver.socketPayload.lobby.SocketBroadcastNewHostInLobbyPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LobbyUtils {

    private final UserToLobbyService userToLobbyService;

    private final ObjectMapper objectMapper;

    private final Map<Socket, Optional<String>> socketMap;

    public void removeUserFromLobbyIfPresent(User user) {
        UserToLobby userToLobby = user.getUserToLobby();
        if (userToLobby != null) {
            Lobby lobby = userToLobby.getLobby();
            List<String> allTokensInLobby = lobby.getAllTokenUsersInLobby();
            allTokensInLobby.forEach(token -> {
                socketMap.forEach((socket, value) -> {
                    if (token.equals(value)) {
                        try {
                            userToLobbyService.removeUserFromLobby(user);
                            SimpleUserDto simpleUser = new SimpleUserDto(user.getId(), user.getUsername());
                            sendDisconnectMessageToUser(socket, simpleUser);
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                    }
                });
            });
        }
    }

    public void sendMessageToAllUsersInLobby(List<String> allTokensInLobby, SimpleUserDto user, EventType eventType) {
        socketMap.forEach((socket, t) -> {
            if (allTokensInLobby.contains(t.get())) {
                try {
                    switch (eventType) {
                        case BROADCAST_USER_CONNECTION_TO_LOBBY -> {
                            SocketBroadcastConnectToLobbyPayload broadcast = SocketBroadcastConnectToLobbyPayload.builder()
                                    .eventType(eventType)
                                    .user(user)
                                    .message(String.format("%s connected", user.getName()))
                                    .build();
                            sendMessage(socket, broadcast);
                        }

                        case BROADCAST_USER_DISCONNECT_FROM_LOBBY -> {
                            SocketBroadcastDisconnectFromLobbyPayload broadcast = SocketBroadcastDisconnectFromLobbyPayload.builder()
                                    .eventType(eventType)
                                    .user(user)
                                    .message(String.format("%s disconnected", user.getName()))
                                    .build();
                            sendMessage(socket, broadcast);
                        }

                        case BROADCAST_NEW_HOST_IN_LOBBY -> {
                            SocketBroadcastNewHostInLobbyPayload broadcast = SocketBroadcastNewHostInLobbyPayload.builder()
                                    .eventType(eventType)
                                    .user(user)
                                    .message(String.format("%s is new host", user.getName()))
                                    .build();
                            sendMessage(socket, broadcast);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }
        });
    }

    private void sendMessage(Socket socket, Object message) throws IOException {
        String response = objectMapper.writeValueAsString(message);
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(response);
        printWriter.flush();
    }

    private void sendDisconnectMessageToUser(Socket socket, SimpleUserDto user) throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        SocketBroadcastDisconnectFromLobbyPayload broadcast = SocketBroadcastDisconnectFromLobbyPayload.builder()
                .eventType(EventType.BROADCAST_USER_DISCONNECT_FROM_LOBBY)
                .user(user)
                .message(String.format("%s disconnected", user.getName()))
                .build();
        String response = objectMapper.writeValueAsString(broadcast);
        printWriter.println(response);
        printWriter.flush();
    }
}
