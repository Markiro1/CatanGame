package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Lobby;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;

import java.net.Socket;
import java.util.List;

public interface SocketMessageSender {

    void broadcastStartGame(int seed, List<Integer> numHexesInMapRow, Lobby lobby);

    void broadcastNewConnect(SocketInfo socketInfo, UserEntity userEntity);

    void broadcastDisconnect(Lobby lobby, UserEntity userEntity);

    void broadcastNewHostIfPresent(Lobby lobby);

    void broadcastUserBuildSettlements(List<Socket> sockets, User user, Integer fieldId);

    void broadcastUserBuildRoad(List<Socket> sockets, User user, Integer fieldId);

    void broadcastUserBuildCity(List<Socket> sockets, User user, Integer fieldId);

    void broadcastUserBuildRoads(List<Socket> sockets, User user, Integer fieldId);

    void broadcastUserTurn(List<Socket> sockets, EventType eventType);

    void broadcastUserWin(List<Socket> sockets, Long randomUserId);

    void broadcastUserDiceRoll(List<Socket> sockets, User user, int firstDiceNum, int secondDiceNum);

    void broadcastUserGetResource(List<Socket> sockets, User user, List<Resource> resources);

    void broadcastRobberRobbery(List<Socket> sockets, Long userId, List<Resource> stolenResources);

    void broadcastUserTrade(
            List<Socket> sockets,
            Long userId,
            Resource sellResource,
            Resource buyResource,
            int requestedAmountOfBuyResource
    );

    void broadcastUserRobbery(List<Socket> sockets, Long robberId, Long victimId, Integer hexId, Resource stealingResource);

    void broadcastBuyCard(List<Socket> sockets, User user, Card card);

    void broadcastUseMonopolyCard(List<Socket> sockets, Long userId, Resource resource);

    void broadcastUseKnightCard(List<Socket> sockets, Long userId, int hexId);

    void broadcastUseRoadBuildingCard(List<Socket> sockets, Long userId);

    void broadcastUseYearOfPlentyCard(List<Socket> sockets, Long userId, List<Resource> resources);

    void broadcastPrepareUserTurn(List<Socket> sockets, Long userId, Integer numOfTurn, EventType eventType);

    void broadcastUserGetLongestRoad(List<Socket> sockets, Long userId);

    void broadcastUserGetLargestArmy(List<Socket> sockets, Long userId);

    void broadcastExchangeResources(
            List<Socket> sockets,
            Long initiatorUserId,
            Long targetUserId,
            int targetAmountOfResource,
            int initiatorAmountOfResource,
            Resource initiatorResource,
            Resource targetResource
    );

    void sendMessage(Socket socket, String message);

    void sendMessage(Socket socket, SocketMessagePayload message);

    void sendResponseExchangeOffer(
            Socket socket,
            Long id,
            int targetAmountOfResource,
            int initiatorAmountOfResource,
            Resource initiatorResource,
            Resource targetResource,
            Long exchangeId
    );

    void broadcastStartRobbery(List<Socket> sockets);
}
