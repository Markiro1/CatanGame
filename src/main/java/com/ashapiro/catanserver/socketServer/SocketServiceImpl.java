package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.dto.HexDto;
import com.ashapiro.catanserver.game.model.Player;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.broadcast.*;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestConnectToLobbyPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketServer.util.Lobby;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SocketServiceImpl implements SocketService {

    private List<SocketInfo> socketInfos = new ArrayList<>();

    private final UserService userService;

    private final LobbyService lobbyService;

    private final UserToLobbyService userToLobbyService;

    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public <T extends SocketMessagePayload> void connectToLobby(Socket clientSocket, T message) {
        if (message instanceof SocketRequestConnectToLobbyPayload) {
            SocketRequestConnectToLobbyPayload requestMessage = (SocketRequestConnectToLobbyPayload) message;
            String token = requestMessage.getToken();

            UserEntity userEntity = userService.findUserByToken(token)
                    .orElseThrow(() -> new NoSuchElementException("User not found with token: " + token));
            LobbyEntity lobbyEntity = userEntity.getUserToLobby().getLobby();

            SocketInfo socketInfo = socketInfos.stream()
                    .filter(si -> si.getSocket().equals(clientSocket))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Socket not found"));

            Player player = new Player(userEntity.getId(), userEntity.getUsername(), false, clientSocket);

            socketInfos.stream()
                    .forEach(si -> {
                        Lobby lobby = si.getLobby();
                        if (lobby == null || !lobby.getId().equals(lobbyEntity.getId())) {
                            lobby = new Lobby(lobbyEntity.getId());
                        }
                        lobby.getPlayerMap().put(clientSocket, player);
                        socketInfo.setLobby(lobby);
                    });

            socketInfo.setToken(token);
            socketInfos.stream()
                    .filter(si -> {
                        Lobby lobby = si.getLobby();
                        return lobby != null && lobby.getId().equals(lobbyEntity.getId());
                    })
                    .forEach(si -> {
                        Lobby lobby = si.getLobby();
                        lobby.getPlayerMap().put(clientSocket, player);
                        socketInfo.setLobby(lobby);

                        sendBroadcastToUsers(
                                si.getSocket(),
                                new SimpleUserDto(userEntity.getId(), userEntity.getUsername()),
                                EventType.BROADCAST_USER_CONNECTION_TO_LOBBY
                        );
                    });

            userEntity.getUserToLobby().setStatus(UserToLobby.ConnectionStatus.CONNECTED);
        }
    }

    @Transactional
    @Override
    public void disconnectFromLobby(Socket clientSocket) {
        String token = socketInfos.stream()
                .filter(si -> si.getSocket().equals(clientSocket))
                .map(SocketInfo::getToken)
                .collect(Collectors.joining());
        if (!token.isEmpty()) {
            try {
                UserEntity userEntity = userService.findUserByToken(token)
                        .orElseThrow(() -> new NoSuchElementException("User not found with token: " + token));

                LobbyEntity lobbyEntity = lobbyService.removeUserFromLobby(userEntity)
                        .orElseThrow(() -> new NoSuchElementException("Lobby not found by user: " + userEntity));

                SocketInfo socketInfo = socketInfos.stream()
                        .filter(si -> si.getSocket().equals(clientSocket))
                        .findFirst()
                        .orElseThrow();
                socketInfos.remove(socketInfo);

                lobbyEntity.getUsersToLobby().stream()
                        .findFirst()
                        .map(UserToLobby::getUser)
                        .ifPresent(newHostUser -> {
                            SimpleUserDto newHost = new SimpleUserDto(newHostUser.getId(), newHostUser.getUsername());
                            socketInfos.forEach(si ->
                                    sendBroadcastToUsers(si.getSocket(), newHost, EventType.BROADCAST_NEW_HOST_IN_LOBBY));
                        });

                socketInfos.stream()
                        .filter(si -> {
                            Lobby lobby = si.getLobby();
                            return lobby != null && lobby.getId().equals(lobbyEntity.getId());
                        })
                        .forEach(si -> {
                            sendBroadcastToUsers(
                                    si.getSocket(),
                                    new SimpleUserDto(userEntity.getId(), userEntity.getUsername()),
                                    EventType.BROADCAST_USER_DISCONNECT_FROM_LOBBY
                            );
                        });

            } catch (NoSuchElementException e) {
                log.error("Failed to remove user from lobby: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeUserFromLobbyIfPresent(String token) {
        UserEntity userEntity = userService.findUserByToken(token)
                .orElseThrow(() -> new NoSuchElementException("User not found by token: " + token));
        UserToLobby userToLobby = userEntity.getUserToLobby();
        if (userToLobby != null) {
            LobbyEntity lobbyEntity = userToLobby.getLobby();
            List<String> allTokensInLobby = lobbyEntity.getAllTokenUsersInLobby();
            allTokensInLobby.forEach(t -> {
                socketInfos.stream()
                        .filter(socketInfo -> socketInfo.getToken().equals(t))
                        .forEach(socketInfo -> {
                            System.out.println(socketInfo.getToken().equals(t));
                            userToLobbyService.removeUserFromLobby(userEntity);
                            sendBroadcastToUsers(
                                    socketInfo.getSocket(),
                                    new SimpleUserDto(userEntity.getId(), userEntity.getUsername()),
                                    EventType.BROADCAST_USER_DISCONNECT_FROM_LOBBY
                            );
                        });
            });
        }
    }

    @Override
    public void updatePlayerReadyStatus(Socket clientSocket) {
        Player player = socketInfos.stream()
                .filter(socketInfo -> socketInfo.getSocket().equals(clientSocket))
                .map(socketInfo -> socketInfo.getLobby().getPlayerMap())
                .flatMap(entry -> entry.entrySet().stream())
                .map(entry -> entry.getValue())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Player not found by socket: " + clientSocket.getInetAddress().getHostAddress()));
        player.setIsReady(true);
    }

    @Override
    public <T extends SocketMessagePayload> void startGame(Socket clientSocket, T message) {
        if (message instanceof SocketRequestStartGamePayload) {
            SocketRequestStartGamePayload socketMessage = (SocketRequestStartGamePayload) message;
            Lobby lobby = socketInfos.stream()
                    .filter(socketInfo -> socketInfo.getSocket().equals(clientSocket))
                    .map(SocketInfo::getLobby)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Lobby not found by socket: " + clientSocket.getInetAddress().getHostAddress()));
            List<HexDto> hexes = lobby.startGame(socketMessage.getNumHexesInMapRow());

            SocketBroadcastStartGamePayload broadcast = new SocketBroadcastStartGamePayload(
                    EventType.BROADCAST_START_GAME,
                    socketMessage.getNumHexesInMapRow(),
                    hexes
            );

            socketInfos.stream()
                    .filter(si -> {
                        Lobby gameLobby = si.getLobby();
                        return gameLobby != null && gameLobby.getId().equals(lobby.getId());
                    })
                    .forEach(si -> {
                        sendMessage(si.getSocket(), broadcast);
                    });
        }
    }

    private void sendBroadcastToUsers(Socket socket, SimpleUserDto user, EventType eventType) {
        StringBuilder message = new StringBuilder();
        SocketBroadcastPayload broadcast = null;
        switch (eventType) {
            case BROADCAST_USER_CONNECTION_TO_LOBBY -> {
                message.append(String.format("%s connected", user.getName()));
                broadcast = new SocketBroadcastConnectToLobbyPayload(eventType, message.toString(), user);
            }
            case BROADCAST_USER_DISCONNECT_FROM_LOBBY -> {
                message.append(String.format("%s disconnected", user.getName()));
                broadcast = new SocketBroadcastDisconnectFromLobbyPayload(eventType, message.toString(), user);
            }
            case BROADCAST_NEW_HOST_IN_LOBBY -> {
                message.append(String.format("%s is new host", user.getName()));
                broadcast = new SocketBroadcastNewHostPayload(eventType, message.toString(), user);
            }
        }
        sendMessage(socket, broadcast);
    }

    private void sendMessage(Socket socket, SocketBroadcastPayload broadcast) {
        try {
            String response = objectMapper.writeValueAsString(broadcast);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(response);
            printWriter.flush();
        } catch (IOException e) {
            log.error("Error send message to user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<SocketInfo> getSocketInfos() {
        return socketInfos;
    }
}
