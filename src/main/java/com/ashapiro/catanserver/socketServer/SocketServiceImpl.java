package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.exceptions.rest.LobbyEntityNotFoundException;
import com.ashapiro.catanserver.exceptions.rest.UserEntityNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.LobbyNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.SocketNotFoundException;
import com.ashapiro.catanserver.exceptions.socket.UserNotFoundException;
import com.ashapiro.catanserver.game.CatanGame;
import com.ashapiro.catanserver.game.enums.GameState;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Lobby;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.game.service.GameService;
import com.ashapiro.catanserver.game.service.GameServiceImpl;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.service.UserToLobbyService;
import com.ashapiro.catanserver.socketServer.payload.request.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.Socket;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class SocketServiceImpl implements SocketService {

    private final UserService userService;

    private final LobbyService lobbyService;

    private final UserToLobbyService userToLobbyService;

    private SocketMessageSender socketMessageSender;

    private GameService gameService;

    private List<SocketInfo> socketInfos;

    public SocketServiceImpl(
            UserService userService,
            LobbyService lobbyService,
            UserToLobbyService userToLobbyService,
            List<SocketInfo> socketInfos
    ) {
        this.userService = userService;
        this.lobbyService = lobbyService;
        this.userToLobbyService = userToLobbyService;
        this.socketMessageSender = SocketMessageSenderImpl.getInstance();
        this.gameService = GameServiceImpl.getInstance();
        this.socketInfos = socketInfos;
    }

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
    public void startGame(Socket clientSocket, SocketRequestStartGamePayload message) {
        UserEntity userEntity = getUserEntity(clientSocket);
        if (checkUserIsHost(userEntity)) {
            Lobby lobby = getLobbyBySocket(clientSocket);
            int seed;
            if (lobby.getCatanGame() == null) {
                seed = lobby.startGame(message.getNumHexesInMapRow());
            } else {
                seed = lobby.getCatanGame().getSeed();
            }
            socketMessageSender.broadcastStartGame(seed, message.getNumHexesInMapRow(), lobby);
        }
    }

    @Override
    public void removeUserFromLobbyIfPresent(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
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
    public void buildSettlement(Socket clientSocket, SocketRequestBuildPayload message) {
        User user = getUserBySocket(clientSocket);
        if (isNotUserTurn(user)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        int fieldId = message.getFieldId();
        gameService.buildSettlement(clientSocket, game, user, fieldId);
    }

    @Override
    public void buildRoad(Socket clientSocket, SocketRequestBuildPayload message) {
        User user = getUserBySocket(clientSocket);
        if (isNotUserTurn(user)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        int fieldId = message.getFieldId();
        gameService.buildRoad(clientSocket, game, user, fieldId);
    }

    @Override
    public void buildCity(Socket clientSocket, SocketRequestBuildPayload message) {
        User user = getUserBySocket(clientSocket);
        if (isNotUserTurn(user)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        int fieldId = message.getFieldId();
        gameService.buildCity(clientSocket, game, user, fieldId);
    }

    @Override
    public void tradeResource(Socket clientSocket, SocketRequestTradeResourcePayload message) {
        CatanGame game = getGameBySocket(clientSocket);
        User user = getUserBySocket(clientSocket);
        if (isNotUserTurn(user)) {
            return;
        }
        int requestedAmountOfBuyResource = message.getRequestedAmountOfBuyResource();
        Resource sellResource = message.getSellResource();
        Resource buyResource = message.getBuyResource();
        gameService.tradeResource(clientSocket, game, user, requestedAmountOfBuyResource, sellResource, buyResource);
    }

    @Override
    public void exchange(Socket clientSocket, SocketRequestExchangePayload socketMessage) {
        if (isNotUserTurn(clientSocket)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        Long exchangeId = socketMessage.getExchangeId();
        boolean isAccept = socketMessage.isAccept();
        gameService.exchange(
                game,
                exchangeId,
                isAccept
        );
    }

    @Override
    public void exchangeResourcesOffer(Socket clientSocket, SocketRequestExchangeResourcesOfferPayload socketMessage) {
        User initiatorUser = getUserBySocket(clientSocket);
        if (isNotUserTurn(initiatorUser)) {
            return;
        }

        Long targetUserId = socketMessage.getTargetUserId();
        int targetAmountOfResource = socketMessage.getTargetAmountOfResource();
        int initiatorAmountOfResource = socketMessage.getInitiatorAmountOfResource();
        Resource targetResource = socketMessage.getTargetResource();
        Resource initiatorResource = socketMessage.getInitiatorResource();

        CatanGame game = getGameBySocket(clientSocket);
        User targetUser = game.getUserById(targetUserId);

        gameService.exchangeResourcesOffer(
                game,
                initiatorUser,
                targetUser,
                initiatorResource,
                initiatorAmountOfResource,
                targetResource,
                targetAmountOfResource
        );
    }

    @Override
    public void updateUserReadyStatus(Socket clientSocket) {
        User user = getUserBySocket(clientSocket);
        CatanGame game = getGameBySocket(clientSocket);
        if (!game.getGameState().equals(GameState.USER_TURN) && !game.getGameState().equals(GameState.WAITING)) {
            return;
        }
        user.setIsReady(true);
    }

    @Override
    public void buyCard(Socket clientSocket) {
        CatanGame catanGame = getGameBySocket(clientSocket);
        User user = getUserBySocket(clientSocket);
        if (isNotUserTurn(user)) {
            return;
        }
        gameService.buyDevCard(catanGame, user);
    }

    @Override
    public void userRobbery(Socket clientSocket, SocketRequestRobberyPayload message) {
        Lobby lobby = getLobbyBySocket(clientSocket);
        User robber = getUserBySocket(clientSocket);
        if (isNotUserTurn(robber)) {
            return;
        }
        Long victimUserId = message.getVictimUserId();
        int hexId = message.getHexId();
        gameService.userRobbery(lobby.getCatanGame(), robber, victimUserId, hexId);
    }

    @Override
    public void useKnightCard(Socket clientSocket, SocketRequestUseKnightCardPayload message) {
        User userWhoUseCard = getUserBySocket(clientSocket);
        if (isNotUserTurn(userWhoUseCard)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        int hexId = message.getHexId();
        gameService.useKnightCard(game, userWhoUseCard, hexId);
    }

    @Override
    public void useMonopolyCard(Socket clientSocket, SocketRequestUseMonopolyCardPayload message) {
        User userWhoUseCard = getUserBySocket(clientSocket);
        if (isNotUserTurn(userWhoUseCard)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        Resource resource = message.getResource();
        gameService.useMonopolyCard(game, userWhoUseCard, resource);
    }

    @Override
    public void useYearOfPlentyCard(Socket clientSocket, SocketRequestUseYearOfPlentyCardPayload message) {
        User userWhoUseCard = getUserBySocket(clientSocket);
        if (isNotUserTurn(userWhoUseCard)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        List<Resource> resources = message.getResources();
        gameService.userYearOfPlentyCard(game, userWhoUseCard, resources);
    }

    @Override
    public void useRoadBuildingCard(Socket clientSocket) {
        User userWhoUseCard = getUserBySocket(clientSocket);
        if (isNotUserTurn(userWhoUseCard)) {
            return;
        }
        CatanGame game = getGameBySocket(clientSocket);
        gameService.useRoadBuildingCard(game, userWhoUseCard);
    }

    public List<SocketInfo> getSocketInfos() {
        return socketInfos;
    }

    private UserEntity getUserEntity(Socket clientSocket) {
        String token = findTokenBySocket(clientSocket).orElse("");
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

    private CatanGame getGameBySocket(Socket clientSocket) {
        String address = clientSocket.getInetAddress().getHostAddress();
        return socketInfos.stream()
                .filter(socketInfo -> socketInfo.getSocket().equals(clientSocket))
                .map(SocketInfo::getLobby)
                .map(Lobby::getCatanGame)
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

    private Optional<String> findTokenBySocket(Socket clientSocket) {
        return socketInfos.stream()
                .filter(si -> si.getSocket().equals(clientSocket))
                .map(SocketInfo::getToken)
                .findFirst();
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

    private boolean isNotUserTurn(Socket clientSocket) {
        User user = getUserBySocket(clientSocket);
        return !user.isHisTurn();
    }

    private boolean isNotUserTurn(User user) {
        return !user.isHisTurn();
    }
}
