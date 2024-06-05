package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.exceptions.rest.LobbyEntityNotFoundException;
import com.ashapiro.catanserver.exceptions.rest.UserEntityNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.LobbyNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.SocketNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.StartGameException;
import com.ashapiro.catanserver.exceptions.socket.UserNotFoundException;
import com.ashapiro.catanserver.game.dto.HexDTO;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.socketServer.dto.UserDTO;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.broadcast.*;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestBuildHousePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestConnectPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketServer.util.Lobby;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SocketServiceImpl implements SocketService {

    private final UserService userService;

    private final LobbyService lobbyService;

    private final UserToLobbyService userToLobbyService;

    private final ObjectMapper objectMapper;

    private List<SocketInfo> socketInfos = new ArrayList<>();

    private Map<EventType, Object> messages;

    @PostConstruct
    public void init() {
        messages = new HashMap<>();
        messages.put(EventType.REQUEST_CONNECT, new SocketRequestConnectPayload());
        messages.put(EventType.REQUEST_START_GAME, new SocketRequestStartGamePayload());
        messages.put(EventType.REQUEST_BUILD_HOUSE, new SocketRequestBuildHousePayload());
    }

    @Transactional
    @Override
    public <T extends SocketMessagePayload> void connectToLobby(Socket clientSocket, T message) {
        SocketRequestConnectPayload socketMessage = (SocketRequestConnectPayload) messages.get(message.getEventType());
        String token = socketMessage.getToken();

        UserEntity userEntity = userService.findUserEntityByToken(token)
                .orElseThrow(() -> new UserEntityNotFoundException(token));
        LobbyEntity lobbyEntity = userEntity.getUserToLobby().getLobby();
        SocketInfo socketInfo = findSocketInfo(clientSocket);
        User user = new User(userEntity.getId(), userEntity.getUsername(), false, clientSocket);

        updateLobbyInfo(socketInfo, lobbyEntity, user);
        socketInfo.setToken(token);
        broadcastUserConnected(socketInfo, userEntity);
        userEntity.getUserToLobby().setStatus(UserToLobby.ConnectionStatus.CONNECTED);
    }

    @Transactional
    @Override
    public void disconnectFromLobby(Socket clientSocket) {
        String token = findTokenBySocket(clientSocket);
        if (!token.isEmpty()) {
            try {
                UserEntity userEntity = userService.findUserEntityByToken(token)
                        .orElseThrow(() -> new UserEntityNotFoundException(token));
                LobbyEntity lobbyEntity = lobbyService.removeUserEntityFromLobby(userEntity)
                        .orElseThrow(() -> new LobbyEntityNotFoundException(userEntity));

                SocketInfo socketInfoToRemove = findSocketInfo(clientSocket);

                removeSocketInfo(socketInfoToRemove);
                broadcastNewHostIfPresent(lobbyEntity);
                broadcastUserDisconnected(lobbyEntity, userEntity);
            } catch (NoSuchElementException e) {
                log.error("Failed to remove user from lobby: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeUserFromLobbyIfPresent(String token) {
        UserEntity userEntity = userService.findUserEntityByToken(token)
                .orElseThrow(() -> new UserEntityNotFoundException(token));
        UserToLobby userToLobby = userEntity.getUserToLobby();

        if (userToLobby != null) {
            LobbyEntity lobbyEntity = userToLobby.getLobby();
            List<String> allTokensInLobby = lobbyEntity.getAllTokenUsersInLobby();
            allTokensInLobby.forEach(t -> removeUserFromLobbyIfTokenMatches(t, userEntity));
        }
    }

    @Override
    public void updateUserReadyStatus(Socket clientSocket) {
        User user = getUserBySocket(clientSocket);
        user.setIsReady(true);
    }

    @Override
    public <T extends SocketMessagePayload> void startGame(Socket clientSocket, T message) {
        SocketRequestStartGamePayload socketMessage = (SocketRequestStartGamePayload) messages.get(message.getEventType());
        Lobby lobby = findLobbyBySocket(clientSocket);

        String token = findTokenBySocket(clientSocket);
        UserEntity userEntity = userService.findUserEntityByToken(token)
                .orElseThrow(() -> new UserEntityNotFoundException(token));
        checkUserIsHost(userEntity);

        List<HexDTO> hexDTOS = lobby.startGame(socketMessage.getNumHexesInMapRow());
        List<UserDTO> userDTOS = mapUserToUserDTO(lobby);
        UserDTO currentUserDTO = mapCurrentUserToUserDTO(clientSocket, lobby);

        SocketBroadcastStartGamePayload broadcast = new SocketBroadcastStartGamePayload(
                EventType.BROADCAST_START_GAME,
                socketMessage.getNumHexesInMapRow(),
                hexDTOS,
                userDTOS,
                currentUserDTO
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

    @Override
    public <T extends SocketMessagePayload> void buildHouse(Socket clientSocket, T message) {
        SocketRequestBuildHousePayload socketMessage = (SocketRequestBuildHousePayload) messages.get(message.getEventType());
        String address = clientSocket.getInetAddress().getHostAddress();

        Lobby lobby = socketInfos.stream()
                .filter(socketInfo -> socketInfo.getSocket().equals(clientSocket))
                .map(SocketInfo::getLobby)
                .findFirst()
                .orElseThrow(() -> new LobbyNotFoundException(address));

        System.out.println("HELLO GUYS, I HAVE HOUSE ON VERTEX WITH ID: " + socketMessage.getVertexId());

    }

    public List<SocketInfo> getSocketInfos() {
        return socketInfos;
    }

    private void checkUserIsHost(UserEntity userEntity) {
        if (!userEntity.getUserToLobby().getIsHost()) {
            String username = userEntity.getUsername();
            throw new StartGameException(username);
        }
    }

    private UserDTO mapCurrentUserToUserDTO(Socket clientSocket, Lobby lobby) {
        User currentUser = lobby.getUserMap().get(clientSocket);
        return new UserDTO(currentUser.getId(), currentUser.getName());
    }

    private List<UserDTO> mapUserToUserDTO(Lobby lobby) {
        return lobby.getUserMap()
                .entrySet()
                .stream()
                .map(entry -> new UserDTO(entry.getValue().getId(), entry.getValue().getName()))
                .toList();
    }

    private Lobby findLobbyBySocket(Socket clientSocket) {
        String address = clientSocket.getInetAddress().getHostAddress();
        return socketInfos.stream()
                .filter(socketInfo -> socketInfo.getSocket().equals(clientSocket))
                .map(SocketInfo::getLobby)
                .findFirst()
                .orElseThrow(() -> new LobbyNotFoundException(address));
    }

    private User getUserBySocket(Socket clientSocket) {
        String address = clientSocket.getInetAddress().getHostAddress();
        return socketInfos.stream()
                .filter(socketInfo -> socketInfo.getSocket().equals(clientSocket))
                .map(socketInfo -> socketInfo.getLobby().getUserMap())
                .flatMap(entry -> entry.entrySet().stream())
                .filter(entry -> entry.getKey().equals(clientSocket))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(address));
    }

    private void removeUserFromLobbyIfTokenMatches(String t, UserEntity userEntity) {
        socketInfos.stream()
                .filter(socketInfo -> socketInfo.getToken().equals(t))
                .forEach(socketInfo -> {
                    userToLobbyService.removeUserFromLobby(userEntity);
                    broadcastUserConnected(socketInfo, userEntity);
                });
    }

    private void removeSocketInfo(SocketInfo socketInfoToRemove) {
        socketInfoToRemove.getLobby().getUserMap()
                .entrySet()
                .removeIf(entry -> entry.getKey().equals(socketInfoToRemove.getSocket()));
        socketInfos.remove(socketInfoToRemove);
    }

    private String findTokenBySocket(Socket clientSocket) {
        return socketInfos.stream()
                .filter(si -> si.getSocket().equals(clientSocket))
                .map(SocketInfo::getToken)
                .findFirst()
                .orElse("");
    }

    private void broadcastUserConnected(SocketInfo socketInfo, UserEntity userEntity) {
        for (SocketInfo si : socketInfos) {
            Lobby lobby = si.getLobby();
            if (lobby != null && lobby.getId().equals(socketInfo.getLobby().getId())) {
                User user = new User(userEntity.getId(), userEntity.getUsername(), false, socketInfo.getSocket());
                lobby.getUserMap().put(socketInfo.getSocket(), user);
                socketInfo.setLobby(lobby);
                sendBroadcastToUsers(si.getSocket(), new SimpleUserDTO(user.getId(), user.getName()), EventType.BROADCAST_USER_CONNECTED);
            }
        }
    }

    private void updateLobbyInfo(SocketInfo socketInfo, LobbyEntity lobbyEntity, User user) {
        for (SocketInfo si : socketInfos) {
            Lobby lobby = si.getLobby();
            if (lobby == null || !lobby.getId().equals(lobbyEntity.getId())) {
                lobby = new Lobby(lobbyEntity.getId());
            }
            lobby.getUserMap().put(socketInfo.getSocket(), user);
            socketInfo.setLobby(lobby);
        }
    }

    private SocketInfo findSocketInfo(Socket clientSocket) {
        String address = clientSocket.getInetAddress().getHostAddress();
        return socketInfos.stream()
                .filter(socketInfo -> socketInfo.getSocket().equals(clientSocket))
                .findFirst()
                .orElseThrow(() -> new SocketNotFoundException(address));
    }

    private void broadcastUserDisconnected(LobbyEntity lobbyEntity, UserEntity userEntity) {
        socketInfos.stream()
                .filter(si -> {
                    Lobby lobby = si.getLobby();
                    return lobby != null && lobby.getId().equals(lobbyEntity.getId());
                })
                .forEach(si -> {
                    sendBroadcastToUsers(
                            si.getSocket(),
                            new SimpleUserDTO(userEntity.getId(), userEntity.getUsername()),
                            EventType.BROADCAST_USER_DISCONNECTED
                    );
                });
    }

    private void broadcastNewHostIfPresent(LobbyEntity lobbyEntity) {
        lobbyEntity.getUsersToLobby().stream()
                .findFirst()
                .map(UserToLobby::getUser)
                .ifPresent(newHostUser -> {
                    SimpleUserDTO newHost = new SimpleUserDTO(newHostUser.getId(), newHostUser.getUsername());
                    socketInfos.forEach(si ->
                            sendBroadcastToUsers(si.getSocket(), newHost, EventType.BROADCAST_NEW_HOST));
                });
    }

    private void sendBroadcastToUsers(Socket socket, SimpleUserDTO user, EventType eventType) {
        StringBuilder message = new StringBuilder();
        SocketBroadcastPayload broadcast = null;
        switch (eventType) {
            case BROADCAST_USER_CONNECTED -> {
                message.append(String.format("%s connected", user.getName()));
                broadcast = new SocketBroadcastConnectToLobbyPayload(eventType, message.toString(), user);
            }
            case BROADCAST_USER_DISCONNECTED -> {
                message.append(String.format("%s disconnected", user.getName()));
                broadcast = new SocketBroadcastDisconnectFromLobbyPayload(eventType, message.toString(), user);
            }
            case BROADCAST_NEW_HOST -> {
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
}
