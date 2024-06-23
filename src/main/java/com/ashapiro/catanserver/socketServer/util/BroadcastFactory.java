package com.ashapiro.catanserver.socketServer.util;

import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.model.Lobby;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.dto.UserDTO;
import com.ashapiro.catanserver.socketServer.payload.SocketBroadcastPayload;
import com.ashapiro.catanserver.socketServer.payload.broadcast.*;

import java.net.Socket;
import java.util.List;

public class BroadcastFactory {

    public SocketBroadcastStartGamePayload createStartGameBroadcast(
            Socket socket,
            int seed,
            List<Integer> numHexesInMapRow,
            Lobby lobby
    ) {
        List<UserDTO> userDTOs = mapUserToUserDTO(lobby);
        UserDTO currentUserDTO = mapCurrentUserToUserDTO(socket, lobby);

        return new SocketBroadcastStartGamePayload(
                EventType.BROADCAST_START_GAME,
                numHexesInMapRow,
                seed,
                userDTOs,
                currentUserDTO
        );
    }

    public SocketBroadcastDisconnectFromLobbyPayload createDisconnectBroadcast(SimpleUserDTO userDto) {
        String message = String.format("%s disconnected", userDto.getName());
        return new SocketBroadcastDisconnectFromLobbyPayload(
                EventType.BROADCAST_USER_DISCONNECTED,
                message,
                userDto
        );
    }

    public SocketBroadcastConnectToLobbyPayload createNewConnectBroadcast(SimpleUserDTO userDto) {
        String message = String.format("%s connected", userDto.getName());
        return new SocketBroadcastConnectToLobbyPayload(
                EventType.BROADCAST_USER_CONNECTED,
                message,
                userDto
        );
    }

    public SocketBroadcastNewHostPayload createNewHostBroadcast(SimpleUserDTO newHost) {
        String message = String.format("%s is new host", newHost.getName());
        return new SocketBroadcastNewHostPayload(
                EventType.BROADCAST_NEW_HOST,
                message,
                newHost
        );
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

    public SocketBroadcastBuildPayload createBuildSettlementBroadcast(SimpleUserDTO userDTO, Integer fieldId) {
        String message = String.format("%s build settlement", userDTO.getName());
        return new SocketBroadcastBuildPayload(
                EventType.BROADCAST_BUILD_SETTLEMENT,
                message,
                userDTO.getId(),
                fieldId
        );
    }

    public SocketBroadcastBuildPayload createBuildRoadBroadcast(SimpleUserDTO userDTO, Integer fieldId) {
        String message = String.format("%s build road", userDTO.getName());
        return new SocketBroadcastBuildPayload(
                EventType.BROADCAST_BUILD_ROAD,
                message,
                userDTO.getId(),
                fieldId
        );
    }

    public SocketBroadcastPayload createBuildCityPayload(SimpleUserDTO userDTO, Integer fieldId) {
        String message = String.format("%s build road", userDTO.getName());
        return new SocketBroadcastBuildPayload(
                EventType.BROADCAST_BUILD_CITY,
                message,
                userDTO.getId(),
                fieldId
        );
    }

    public DefaultSocketBroadcastPayload createUserTurnBroadcast(EventType eventType) {
        return new DefaultSocketBroadcastPayload(eventType);
    }

    public SocketBroadcastPrepareUserTurnPayload createPrepareUserTurnBroadcast(Long userId, Integer numOfTurn, EventType eventType) {
        return new SocketBroadcastPrepareUserTurnPayload(
                eventType,
                userId,
                numOfTurn
        );
    }

    public SocketBroadcastUserWinPayload createUserWinBroadcast(Long randomUserId) {
        return new SocketBroadcastUserWinPayload(
                EventType.BROADCAST_USER_WIN,
                randomUserId
        );
    }

    public SocketBroadcastPayload createDiceRollBroadcast(User user, int firstDiceNum, int secondDiceNum) {
        return new SocketBroadcastDiceThrowPayload(
                EventType.BROADCAST_DICE_THROW,
                user.getId(),
                firstDiceNum,
                secondDiceNum
        );
    }

    public SocketBroadcastPayload createUserGetResourceBroadcast(Long userId, List<Resource> resources) {
        return new SocketBroadcastUserResourcesPayload(
                EventType.BROADCAST_USER_GET_RESOURCE,
                userId,
                resources
        );
    }

    public SocketBroadcastPayload createUserRobberRobberyBroadcast(Long userId, List<Resource> resources) {
        return new SocketBroadcastUserResourcesPayload(
                EventType.BROADCAST_ROBBER_ROBBERY,
                userId,
                resources
        );
    }

    public SocketBroadcastPayload createUserTradeBroadcast(
            Long userId,
            Resource sellResource,
            Resource buyResource,
            int requestedAmountOfBuyResource
    ) {
        return new SocketBroadcastUserTrade(
                EventType.BROADCAST_USER_TRADE,
                userId,
                sellResource,
                buyResource,
                requestedAmountOfBuyResource
        );
    }

    public SocketBroadcastPayload createUserRobberyBroadcast(Long robberId, Long victimId, Integer hexId, Resource stealingResource) {
        return new SocketBroadcastUserRobberyPayload(
                EventType.BROADCAST_USER_ROBBERY,
                robberId,
                victimId,
                hexId,
                stealingResource
        );
    }

    public SocketBroadcastPayload createStartRobberyBroadcast() {
        return new DefaultSocketBroadcastPayload(EventType.BROADCAST_ROBBERY_START);
    }

    public SocketBroadcastPayload createBuyCardBroadcast(Long userId, Card card) {
        return new SocketBroadcastBuyCardPayload(
                EventType.BROADCAST_BUY_CARD,
                userId,
                card
        );
    }

    public SocketBroadcastPayload createUseMonopolyCard(Long userId, Resource resource) {
        return new SocketBroadcastUseMonopolyCardPayload(
                EventType.BROADCAST_USE_MONOPOLY_CARD,
                userId,
                resource
        );
    }

    public SocketBroadcastPayload createUseRoadBuildingCard(Long userId) {
        return new SocketBroadcastUseRoadBuildingCardPayload(
                EventType.BROADCAST_USE_ROAD_BUILDING_CARD,
                userId
        );
    }

    public SocketBroadcastPayload createUseYearOfPlentyCard(Long userId, List<Resource> resources) {
        return new SocketBroadcastUseYearOfPlentyCardPayload(
                EventType.BROADCAST_USE_YEAR_OF_PLENTY_CARD,
                userId,
                resources
        );
    }

    public SocketBroadcastPayload createUseKnightCard(Long userId, int hexId) {
        return new SocketBroadcastUseKnightCardPayload(
                EventType.BROADCAST_USE_KNIGHT_CARD,
                userId,
                hexId
        );
    }

    public SocketBroadcastPayload createUserGetLongestRoadBroadcast(Long userId) {
        return new SocketBroadcastUserGetLargestCardPayload(
                EventType.BROADCAST_USER_GET_LONGEST_ROAD,
                userId
        );
    }

    public SocketBroadcastPayload createUserGetLargestArmyBroadcast(Long userId) {
        return new SocketBroadcastUserGetLargestCardPayload(
                EventType.BROADCAST_USER_GET_LARGEST_ARMY,
                userId
        );
    }

    public SocketBroadcastPayload createExchangeResourcesBroadcast(
            Long initiatorUserId,
            Long targetUserId,
            int targetAmountOfResource,
            int initiatorAmountOfResource,
            Resource initiatorResource,
            Resource targetResource
    ) {
        return new SocketBroadcastExchangePayload(
                EventType.BROADCAST_EXCHANGE,
                initiatorUserId,
                targetUserId,
                targetAmountOfResource,
                initiatorAmountOfResource,
                initiatorResource,
                targetResource
        );
    }
}
