package com.ashapiro.catanserver.socketServer.util;

import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.User;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.dto.UserDTO;
import com.ashapiro.catanserver.socketServer.payload.broadcast.*;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;

import java.net.Socket;
import java.util.List;

public class BroadcastFactory {

    public SocketBroadcastStartGamePayload createStartGameBroadcast(
            Socket socket,
            SocketRequestStartGamePayload socketPayload,
            Lobby lobby
    ) {
        List<Integer> numHexesInMapRow = socketPayload.getNumHexesInMapRow();
        int seed = lobby.startGame(numHexesInMapRow);
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

    public SocketBroadcastUserTurnPayload createUserTurnBroadcast(Long userId, Integer numOfTurn, EventType eventType) {
        return new SocketBroadcastUserTurnPayload(
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
        return new SocketBroadcastUserGetResourcesPayload(
                EventType.BROADCAST_USER_GET_RESOURCE,
                userId,
                resources
        );
    }

    public SocketBroadcastPayload createUserTradeBroadcast(
            Long userId,
            Resource incomingResource,
            Resource outgoingResource,
            int requestedCountOfOutgoingResource
    ) {
        return new SocketBroadcastUserTrade(
                EventType.BROADCAST_USER_TRADE,
                userId,
                incomingResource,
                outgoingResource,
                requestedCountOfOutgoingResource
        );
    }
}
