package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Lobby;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.broadcast.DefaultSocketBroadcastPayload;
import com.ashapiro.catanserver.socketServer.payload.broadcast.SocketBroadcastNewHostPayload;
import com.ashapiro.catanserver.socketServer.payload.response.SocketResponseExchangeResourceOfferPayload;
import com.ashapiro.catanserver.socketServer.util.BroadcastFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

@Slf4j
public class SocketMessageSenderImpl implements SocketMessageSender {

    private static SocketMessageSenderImpl instance;

    private ObjectMapper objectMapper;

    private SocketMessageSenderImpl() {
        this.objectMapper = new ObjectMapper();
    }

    public static synchronized SocketMessageSenderImpl getInstance() {
        if (instance == null) {
            instance = new SocketMessageSenderImpl();
        }
        return instance;
    }

    private BroadcastFactory broadcastFactory = new BroadcastFactory();

    @Override
    public void broadcastStartGame(int seed, List<Integer> numHexesInMapRow, Lobby lobby) {
        lobby.getUserMap().keySet().forEach(socket -> {
            SocketBroadcastPayload broadcast = broadcastFactory.createStartGameBroadcast(socket, seed, numHexesInMapRow, lobby);
            sendBroadcastMessage(socket, broadcast);
        });
    }

    @Override
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
        sendBroadcastToAllInLobby(lobby, broadcast);
    }

    @Override
    public void broadcastDisconnect(Lobby lobby, UserEntity userEntity) {
        SimpleUserDTO userDTO = new SimpleUserDTO(userEntity.getId(), userEntity.getUsername());
        SocketBroadcastPayload broadcast = broadcastFactory.createDisconnectBroadcast(userDTO);
        sendBroadcastToAllInLobby(lobby, broadcast);
    }

    @Override
    public void broadcastNewHostIfPresent(Lobby lobby) {
        lobby.getUserMap().values().stream()
                .findFirst()
                .ifPresent(newHostUser -> {
                    SimpleUserDTO newHost = new SimpleUserDTO(newHostUser.getId(), newHostUser.getName());
                    SocketBroadcastNewHostPayload broadcast = broadcastFactory.createNewHostBroadcast(newHost);
                    sendBroadcastToAllInLobby(lobby, broadcast);
                });
    }

    @Override
    public void broadcastUserBuildSettlements(List<Socket> sockets, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildSettlementBroadcast(userDTO, fieldId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserBuildRoad(List<Socket> sockets, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildRoadBroadcast(userDTO, fieldId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserBuildCity(List<Socket> sockets, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildCityPayload(userDTO, fieldId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserBuildRoads(List<Socket> sockets, User user, Integer fieldId) {
        SimpleUserDTO userDTO = new SimpleUserDTO(user.getId(), user.getName());
        SocketBroadcastPayload broadcast = broadcastFactory.createBuildRoadBroadcast(userDTO, fieldId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserTurn(List<Socket> sockets, EventType eventType) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserTurnBroadcast(eventType);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserWin(List<Socket> sockets, Long randomUserId) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserWinBroadcast(randomUserId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserDiceRoll(List<Socket> sockets, User user, int firstDiceNum, int secondDiceNum) {
        SocketBroadcastPayload broadcast = broadcastFactory.createDiceRollBroadcast(user, firstDiceNum, secondDiceNum);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserGetResource(List<Socket> sockets, User user, List<Resource> resources) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserGetResourceBroadcast(user.getId(), resources);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastRobberRobbery(List<Socket> sockets, Long userId, List<Resource> stolenResources) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserRobberRobberyBroadcast(userId, stolenResources);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserTrade(
            List<Socket> sockets,
            Long userId,
            Resource sellResource,
            Resource buyResource,
            int requestedAmountOfBuyResource
    ) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserTradeBroadcast(
                userId,
                sellResource,
                buyResource,
                requestedAmountOfBuyResource);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserRobbery(List<Socket> sockets, Long robberId, Long victimId, Integer hexId, Resource stealingResource) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserRobberyBroadcast(robberId, victimId, hexId, stealingResource);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastBuyCard(List<Socket> sockets, User user, Card card) {
        SocketBroadcastPayload broadcastForUserWhoBuy = broadcastFactory.createBuyCardBroadcast(user.getId(), card);
        SocketBroadcastPayload broadcastForOthers = broadcastFactory.createBuyCardBroadcast(user.getId(), Card.UNKNOWN);
        sockets.forEach(socket -> {
            if (socket.equals(user.getSocket())) {
                sendBroadcastMessage(socket, broadcastForUserWhoBuy);
            } else {
                sendBroadcastMessage(socket, broadcastForOthers);
            }
        });
    }

    @Override
    public void broadcastUseMonopolyCard(List<Socket> sockets, Long userId, Resource resource) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUseMonopolyCard(userId, resource);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUseKnightCard(List<Socket> sockets, Long userId, int hexId) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUseKnightCard(userId, hexId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUseRoadBuildingCard(List<Socket> sockets, Long userId) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUseRoadBuildingCard(userId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUseYearOfPlentyCard(List<Socket> sockets, Long userId, List<Resource> resources) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUseYearOfPlentyCard(userId, resources);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastPrepareUserTurn(List<Socket> sockets, Long userId, Integer numOfTurn, EventType eventType) {
        SocketBroadcastPayload broadcast = broadcastFactory.createPrepareUserTurnBroadcast(userId, numOfTurn, eventType);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserGetLongestRoad(List<Socket> sockets, Long userId) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserGetLongestRoadBroadcast(userId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastUserGetLargestArmy(List<Socket> sockets, Long userId) {
        SocketBroadcastPayload broadcast = broadcastFactory.createUserGetLargestArmyBroadcast(userId);
        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void broadcastExchangeResources(
            List<Socket> sockets,
            Long initiatorUserId,
            Long targetUserId,
            int targetAmountOfResource,
            int initiatorAmountOfResource,
            Resource initiatorResource,
            Resource targetResource) {
        SocketBroadcastPayload broadcast = broadcastFactory.createExchangeResourcesBroadcast(
                initiatorUserId,
                targetUserId,
                targetAmountOfResource,
                initiatorAmountOfResource,
                initiatorResource,
                targetResource
        );

        sendBroadcastToSockets(sockets, broadcast);
    }

    @Override
    public void sendMessage(Socket socket, String message) {
        if (isSocketClosed(socket)) {
            return;
        }
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(message);
            printWriter.flush();
        } catch (IOException e) {
            log.error("Error send message to user: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void sendMessage(Socket socket, SocketMessagePayload message) {
        if (isSocketClosed(socket)) {
            return;
        }
        try {
            String response = objectMapper.writeValueAsString(message);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(response);
            printWriter.flush();
        } catch (IOException e) {
            log.error("Error send message to user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendResponseExchangeOffer(
            Socket socket,
            Long initiatorUserId,
            int targetAmountOfResource,
            int initiatorAmountOfResource,
            Resource initiatorResource,
            Resource targetResource,
            Long exchangeId
    ) {
        SocketResponseExchangeResourceOfferPayload message = new SocketResponseExchangeResourceOfferPayload(
                EventType.RESPONSE_EXCHANGE_OFFER,
                initiatorUserId,
                targetAmountOfResource,
                initiatorAmountOfResource,
                initiatorResource,
                targetResource,
                exchangeId
        );

        sendMessage(socket, message);
    }

    @Override
    public void broadcastStartRobbery(List<Socket> sockets) {
        DefaultSocketBroadcastPayload broadcast = new DefaultSocketBroadcastPayload(EventType.BROADCAST_ROBBERY_START);
        sendBroadcastToSockets(sockets, broadcast);
    }

    private void sendBroadcastToAllInLobby(Lobby lobby, SocketBroadcastPayload broadcast) {
        lobby.getUserMap().keySet().forEach(socket -> sendBroadcastMessage(socket, broadcast));
    }

    private void sendBroadcastToSockets(List<Socket> sockets, SocketBroadcastPayload broadcast) {
        sockets.forEach(socket -> sendBroadcastMessage(socket, broadcast));
    }

    private void sendBroadcastMessage(Socket socket, SocketBroadcastPayload broadcast) {
        if (socket == null || socket.isClosed()) {
            return;
        }
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

    private boolean isSocketClosed(Socket socket) {
        return socket.isClosed();
    }
}
