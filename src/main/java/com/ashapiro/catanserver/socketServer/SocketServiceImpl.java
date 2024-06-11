package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.exceptions.rest.LobbyEntityNotFoundException;
import com.ashapiro.catanserver.exceptions.rest.UserEntityNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.LobbyNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.SocketNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.UserNotFoundException;
import com.ashapiro.catanserver.game.*;
import com.ashapiro.catanserver.game.enums.EdgeBuildingType;
import com.ashapiro.catanserver.game.enums.GameState;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.enums.VertexBuildingType;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.Vertex;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestBuildPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestConnectPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestTradeResourcePayload;
import com.ashapiro.catanserver.socketServer.util.Lobby;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SocketServiceImpl implements SocketService {

    private final UserService userService;

    private final LobbyService lobbyService;

    private final UserToLobbyService userToLobbyService;

    private final SocketMessageSender socketMessageSender;

    private List<SocketInfo> socketInfos = new ArrayList<>();

    @Transactional
    @Override
    public void connectToLobby(Socket clientSocket, SocketRequestConnectPayload message) {
        String token = message.getToken();

        UserEntity userEntity = userService.findUserEntityByToken(token)
                .orElseThrow(() -> new UserEntityNotFoundException(token));
        LobbyEntity lobbyEntity = userEntity.getUserToLobby().getLobby();
        SocketInfo socketInfo = findSocketInfo(clientSocket);
        User user = new User(userEntity.getId(), userEntity.getUsername(), clientSocket);

        updateLobbyInfo(socketInfo, lobbyEntity, user);
        socketInfo.setToken(token);
        userEntity.getUserToLobby().setStatus(UserToLobby.ConnectionStatus.CONNECTED);
        socketMessageSender.broadcastNewConnect(socketInfo, userEntity);

    }

    @Override
    public void startGame(Socket clientSocket, SocketRequestStartGamePayload message) {
        Lobby lobby = getLobbyBySocket(clientSocket);
        UserEntity userEntity = getUserEntity(clientSocket);
        if (checkUserIsHost(userEntity)) {
            socketMessageSender.broadcastStartGame(clientSocket, message, lobby);
        }
    }

    @Override
    public void buildSettlement(Socket clientSocket, SocketRequestBuildPayload message) {
        Lobby lobby = getLobbyBySocket(clientSocket);
        User user = getUserBySocket(clientSocket);
        CatanGame catanGame = lobby.getCatanGame();
        Vertex currentVertex = catanGame.getVertices().get(message.getFieldId());

        if (canBuiltSettlement(currentVertex, user, catanGame)) {
            if (!catanGame.getGameState().equals(GameState.PREPARATION_BUILD_SETTLEMENTS)) {
                transferResourceToBank(catanGame.getBank(), user, ResourceCost.getSettlementCost());
            }
            currentVertex.setUser(user);
            currentVertex.setType(VertexBuildingType.SETTLEMENT);
            socketMessageSender.broadcastUserBuildSettlements(lobby, user, currentVertex.getId());
        } else {
            socketMessageSender.sendMessage(clientSocket, "This vertex is already occupied");
        }
    }

    @Override
    public void buildRoad(Socket clientSocket, SocketRequestBuildPayload message) {
        Lobby lobby = getLobbyBySocket(clientSocket);
        User user = getUserBySocket(clientSocket);
        CatanGame catanGame = lobby.getCatanGame();
        Edge currentEdge = catanGame.getEdges().get(message.getFieldId());

        if (canBuildRoad(currentEdge, user, catanGame)) {
            if (!catanGame.getGameState().equals(GameState.PREPARATION_BUILD_ROADS)) {
                transferResourceToBank(lobby.getCatanGame().getBank(), user, ResourceCost.getRoadCost());
            }
            currentEdge.setUser(user);
            currentEdge.setType(EdgeBuildingType.ROAD);
            socketMessageSender.broadcastUserBuildRoad(lobby, user, currentEdge.getId());
        } else {
            socketMessageSender.sendMessage(clientSocket, "This edge is already occupied");
        }
    }

    @Override
    public void buildCity(Socket clientSocket, SocketRequestBuildPayload message) {
        Lobby lobby = getLobbyBySocket(clientSocket);
        User user = getUserBySocket(clientSocket);
        CatanGame catanGame = lobby.getCatanGame();
        Vertex currentVertex = catanGame.getVertices().get(message.getFieldId());

        if (canBuildCity(currentVertex, user, catanGame)) {
            transferResourceToBank(lobby.getCatanGame().getBank(), user, ResourceCost.getCityCost());
        }
        currentVertex.setType(VertexBuildingType.CITY);
        socketMessageSender.broadcastUserBuildCity(lobby, user, currentVertex.getId());
    }

    @Override
    public void tradeResource(Socket clientSocket, SocketRequestTradeResourcePayload message) {
        Lobby lobby = getLobbyBySocket(clientSocket);
        User user = getUserBySocket(clientSocket);
        Bank bank = lobby.getCatanGame().getBank();
        if (bankContainsResources(bank, clientSocket, message) && userContainsResource(user, clientSocket, message)) {
            bank.tradeResource(user, message);
            Resource incomingResource = message.getIncomingResource();
            Resource outgointResource = message.getOutgoingResource();
            int requestedCountOfOutgoingResource = message.getRequestedCountOfOutgoingResource();
            socketMessageSender.broadcastUserTrade(lobby, user.getId(), incomingResource, outgointResource, requestedCountOfOutgoingResource);
        }
    }

    @Transactional
    @Override
    public void disconnectFromLobby(Socket clientSocket) {
        try {
            UserEntity userEntity = getUserEntity(clientSocket);
            lobbyService.removeUserEntityFromLobby(userEntity)
                    .orElseThrow(() -> new LobbyEntityNotFoundException(userEntity));

            SocketInfo socketInfoToRemove = findSocketInfo(clientSocket);
            Lobby lobby = socketInfoToRemove.getLobby();

            removeSocketInfo(socketInfoToRemove);
            socketMessageSender.broadcastNewHostIfPresent(lobby);
            socketMessageSender.broadcastDisconnect(lobby, userEntity);
        } catch (NoSuchElementException e) {
            log.error("Failed to remove user from lobby: " + e.getMessage());
            e.printStackTrace();
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

    public List<SocketInfo> getSocketInfos() {
        return socketInfos;
    }

    private boolean userContainsResource(User user, Socket clientSocket, SocketRequestTradeResourcePayload message) {
        Map<Resource, Integer> userInventory = user.getInventory();
        Resource incomingResource = message.getIncomingResource();
        int availableUserResources = userInventory.get(incomingResource);
        boolean userContainsResources = availableUserResources >= 4;
        if (!userContainsResources) {
            socketMessageSender.sendMessage(clientSocket,
                    String.format("%s does not have enough resources with type ^%s", user.getName(), incomingResource));
        }
        return userContainsResources;
    }


    private boolean bankContainsResources(Bank bank, Socket clientSocket, SocketRequestTradeResourcePayload message) {
        int desiredCountOfOutgoingResource = message.getRequestedCountOfOutgoingResource();
        Resource outgoingResource = message.getOutgoingResource();
        int availableResource = bank.getStorage().getOrDefault(outgoingResource, 0);
        boolean bankContainsResources = availableResource >= desiredCountOfOutgoingResource;
        if (!bankContainsResources) {
            socketMessageSender.sendMessage(clientSocket,
                    String.format("The bank not have enough resource with type %s in storage", outgoingResource));
        }
        return bankContainsResources;
    }


    private boolean canBuildRoad(Edge currentEdge, User user, CatanGame catanGame) {
        if (!user.isHisTurn()) {
            return false;
        }

        if (catanGame.getGameState().equals(GameState.PREPARATION_BUILD_ROADS)) {
            long countOfRoads = catanGame.getEdges().stream()
                    .filter(edge -> edge.getUser() != null && edge.getUser().equals(user))
                    .count();
            return countOfRoads < catanGame.getNumOfTurn() && currentEdge.getType().equals(EdgeBuildingType.NONE);
        }

        Map<Resource, Integer> roadCost = ResourceCost.getRoadCost();
        boolean hasRequiresResources = hasRequiredResources(user, roadCost);

        return currentEdge.getType().equals(EdgeBuildingType.NONE)
                && hasAvailableEdgeForUser(currentEdge, user, catanGame)
                && hasRequiresResources;
    }

    private boolean canBuiltSettlement(Vertex currentVertex, User user, CatanGame catanGame) {
        if (!user.isHisTurn()) {
            return false;
        }
        if (catanGame.getGameState().equals(GameState.PREPARATION_BUILD_SETTLEMENTS)) {
            long countOfSettlements = catanGame.getVertices().stream()
                    .filter(vertex -> vertex.getUser() != null && vertex.getUser().equals(user))
                    .count();
            return countOfSettlements < catanGame.getNumOfTurn() && currentVertex.getType().equals(VertexBuildingType.NONE);
        }

        Map<Resource, Integer> settlementCost = ResourceCost.getSettlementCost();
        boolean hasRequiredResources = hasRequiredResources(user, settlementCost);

        return currentVertex.getType().equals(VertexBuildingType.NONE)
                && hasAvailableVertexForUser(currentVertex, user, catanGame)
                && hasRequiredResources;
    }

    private boolean canBuildCity(Vertex currentVertex, User user, CatanGame catanGame) {
        if (!user.isHisTurn()) {
            return false;
        }

        Map<Resource, Integer> cityCost = ResourceCost.getCityCost();
        boolean hasRequiresResources = hasRequiredResources(user, cityCost);

        return currentVertex.getType().equals(VertexBuildingType.CITY)
                && hasAvailableVertexToBuildCity(currentVertex, user, catanGame)
                && hasRequiresResources;
    }

    private void transferResourceToBank(Bank bank, User user, Map<Resource, Integer> resourceCost) {
        for (Map.Entry<Resource, Integer> entry : resourceCost.entrySet()) {
            Resource resource = entry.getKey();
            int cost = entry.getValue();

            user.getInventory().put(resource, user.getInventory().get(resource) - cost);
            bank.getStorage().put(resource, bank.getStorage().getOrDefault(resource, 0) + cost);
        }
    }

    private boolean hasAvailableVertexToBuildCity(Vertex currentVertex, User user, CatanGame catanGame) {
        return MapUtil.getUserVertices(user, catanGame.getVertices()).contains(currentVertex);
    }

    private boolean hasAvailableVertexForUser(Vertex currentVertex, User user, CatanGame catanGame) {
        List<Edge> edges = catanGame.getEdges();
        return MapUtil.getAvailableVerticesForUser(user, edges).contains(currentVertex);
    }

    private boolean hasAvailableEdgeForUser(Edge currentEdge, User user, CatanGame catanGame) {
        List<Vertex> vertices = catanGame.getVertices();
        List<Edge> edges = catanGame.getEdges();
        return MapUtil.getAvailableEdgesForUser(user, vertices, edges).contains(currentEdge);

    }

    private boolean hasRequiredResources(User user, Map<Resource, Integer> cost) {
        return cost.entrySet().stream()
                .allMatch(entry -> user.getInventory().getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    private UserEntity getUserEntity(Socket clientSocket) {
        String token = findTokenBySocket(clientSocket);
        if (!token.isEmpty()) {
            return userService.findUserEntityByToken(token)
                    .orElseThrow(() -> new UserEntityNotFoundException(token));
        }
        throw new UserEntityNotFoundException(token);
    }

    private boolean checkUserIsHost(UserEntity userEntity) {
        return userEntity.getUserToLobby().getIsHost();
    }

    private Lobby getLobbyBySocket(Socket clientSocket) {
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
                    socketMessageSender.broadcastNewConnect(socketInfo, userEntity);
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
}
