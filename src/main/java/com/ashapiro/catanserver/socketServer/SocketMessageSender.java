package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.User;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.payload.broadcast.SocketBroadcastNewHostPayload;
import com.ashapiro.catanserver.socketServer.payload.broadcast.SocketBroadcastPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketServer.util.BroadcastFactory;
import com.ashapiro.catanserver.socketServer.util.Lobby;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketMessageSender {

    private final ObjectMapper objectMapper;

    private BroadcastFactory broadcastFactory = new BroadcastFactory();

    public void broadcastStartGame(Socket clientSocket, SocketRequestStartGamePayload socketMessage, Lobby lobby) {
        SocketBroadcastPayload broadcast =
                broadcastFactory.createStartGameBroadcast(clientSocket, socketMessage, lobby);
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastNewConnect(SocketInfo socketInfo, UserEntity userEntity) {
        Lobby lobby = socketInfo.getLobby();
        if (lobby == null) {
            return;
        }
        User newUser = new User(userEntity.getId(), userEntity.getUsername(), socketInfo.getSocket());
        SimpleUserDTO userDTO = new SimpleUserDTO(newUser.getId(), newUser.getName());

        lobby.getUserMap().put(socketInfo.getSocket(), newUser);
        socketInfo.setLobby(lobby);
        SocketBroadcastPayload broadcast = broadcastFactory.createNewConnectBroadcast(userDTO);
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastDisconnect(Lobby lobby, UserEntity userEntity) {
        SimpleUserDTO userDTO = new SimpleUserDTO(userEntity.getId(), userEntity.getUsername());
        SocketBroadcastPayload broadcast = broadcastFactory.createDisconnectBroadcast(userDTO);
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastNewHostIfPresent(Lobby lobby) {
        lobby.getUserMap().values().stream()
                .findFirst()
                .ifPresent(newHostUser -> {
                    SimpleUserDTO newHost = new SimpleUserDTO(newHostUser.getId(), newHostUser.getName());
                    SocketBroadcastNewHostPayload broadcast = broadcastFactory.createNewHostBroadcast(newHost);
                    lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
                });

    }

    public void broadcastUserBuildSettlements(Lobby lobby, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildSettlementBroadcast(userDTO, fieldId);
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserBuildSettlements(List<Socket> sockets, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildSettlementBroadcast(userDTO, fieldId);
        sockets.stream().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserBuildRoad(Lobby lobby, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildRoadBroadcast(userDTO, fieldId);
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserBuildCity(Lobby lobby, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildCityPayload(userDTO, fieldId);
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserBuildRoads(List<Socket> sockets, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildRoadBroadcast(userDTO, fieldId);
        sockets.stream().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserTurn(List<Socket> sockets, Long userId, Integer numOfTurn, EventType eventType) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserTurnBroadcast(userId, numOfTurn, eventType);
        sockets.stream().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserWin(List<Socket> sockets, Long randomUserId) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserWinBroadcast(randomUserId);
        sockets.stream().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserDiceRoll(List<Socket> sockets, User user, int firstDiceNum, int secondDiceNum) {
        SocketBroadcastPayload broadcast = broadcastFactory.createDiceRollBroadcast(user, firstDiceNum, secondDiceNum);
        sockets.stream().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserGetResource(List<Socket> sockets, User user, List<Resource> resources) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserGetResourceBroadcast(user.getId(), resources);
        sockets.stream().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void broadcastUserTrade(
            Lobby lobby,
            Long userId,
            Resource incomingResource,
            Resource outgointResource,
            int requestedCountOfOutgoingResource
    ) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserTradeBroadcast(
                userId,
                incomingResource,
                outgointResource,
                requestedCountOfOutgoingResource);
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessageIfOpen(socket, broadcast));
    }

    public void sendMessage(Socket socket, String message) {
        if (!socket.isClosed()) {
            try {
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println(message);
                printWriter.flush();
            } catch (IOException e) {
                log.error("Error send message to user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void sendBroadcastMessageIfOpen(Socket socket, SocketBroadcastPayload broadcast) {
        if (socket != null && !socket.isClosed()) {
            sendBroadcastMessage(socket, broadcast);
        }
    }

    private void sendBroadcastMessage(Socket socket, SocketBroadcastPayload broadcast) {
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
