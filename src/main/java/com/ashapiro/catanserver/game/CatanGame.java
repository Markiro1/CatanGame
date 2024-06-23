package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.EdgeBuildingType;
import com.ashapiro.catanserver.game.enums.GameState;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.enums.VertexBuildingType;
import com.ashapiro.catanserver.game.model.*;
import com.ashapiro.catanserver.game.service.BankService;
import com.ashapiro.catanserver.game.service.BankServiceImpl;
import com.ashapiro.catanserver.game.service.GameService;
import com.ashapiro.catanserver.game.service.GameServiceImpl;
import com.ashapiro.catanserver.socketServer.SocketMessageSender;
import com.ashapiro.catanserver.socketServer.SocketMessageSenderImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
@Slf4j
public class CatanGame extends Thread {

    private GameState gameState;

    private List<User> users;

    private List<Hex> hexes;

    private List<Edge> edges;

    private List<Vertex> vertices;

    private int seed;

    private Bank bank;

    private Lobby lobby;

    private GameService gameService;

    private BankService bankService;

    private SocketMessageSender socketMessageSender;

    private static final int TURN_DURATION = 60000;

    private Integer numOfTurn = 1;

    public CatanGame(List<User> users, List<Hex> hexes, List<Edge> edges, List<Vertex> vertices, int seed, Lobby lobby) {
        this.users = new ArrayList<>(users);
        this.bank = new Bank();
        this.hexes = hexes;
        this.edges = edges;
        this.vertices = vertices;
        this.seed = seed;
        this.lobby = lobby;
        this.gameState = GameState.WAITING;
        this.gameService = GameServiceImpl.getInstance();
        this.bankService = BankServiceImpl.getInstance();
        this.socketMessageSender = SocketMessageSenderImpl.getInstance();
        start();
    }

    @Override
    public void run() {
        try {
            waitForUsersReady();
            gamePreparation();
            gameLoop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Game manager interrupted");
        }
    }

    private void gameLoop() throws InterruptedException {
        log.info("STARTED MAIN GAME LOOP AFTER GAME PREPARATION");
        List<Socket> sockets = getUsersSocket();
        while (!gameState.equals(GameState.FINISHED)) {
            for (numOfTurn = 1; numOfTurn <= 1000; numOfTurn++) {
                for (User currentUser : users) {
                    executeTurn(sockets, currentUser);
                    if (gameState.equals(GameState.FINISHED)) {
                        break;
                    }
                }
                if (gameState.equals(GameState.FINISHED)) {
                    break;
                }
            }
        }
    }

    private void gamePreparation() {
        gameState = GameState.PREPARATION_BUILD_SETTLEMENTS;
        List<Socket> sockets = getUsersSocket();
        for (numOfTurn = 1; numOfTurn <= 2; numOfTurn++) {
            executePreparationTurnForUsers(
                    sockets,
                    numOfTurn,
                    EventType.BROADCAST_PREPARATION_USER_TURN_BUILD_SETTLEMENTS,
                    this::getUserVertices,
                    this::assignRandomSettlementToUser
            );
        }

        gameState = GameState.PREPARATION_BUILD_ROADS;
        for (numOfTurn = 1; numOfTurn <= 2; numOfTurn++) {
            executePreparationTurnForUsers(
                    sockets,
                    numOfTurn,
                    EventType.BROADCAST_PREPARATION_USER_TURN_BUILD_ROADS,
                    this::getUserEdges,
                    this::assignRandomRoadToUser
            );
        }

        gameState = GameState.USER_TURN;
    }

    private void executeTurn(List<Socket> sockets, User currentUser) {
        currentUser.setIsReady(false);
        gameState = GameState.PREPARING_USER_TURN;
        startTurn(sockets, currentUser, numOfTurn, EventType.BROADCAST_PREPARE_USER_TURN);
        int resultOfRoll = gameService.diceRoll(currentUser, sockets);
        if (resultOfRoll == 7) {
            gameState = GameState.ROBBERY;
            socketMessageSender.broadcastStartRobbery(sockets);
            randomStealResources(sockets);
            boolean isDidAction = waitForUserRobbery(currentUser);
            if (!isDidAction) {
                robberPlacement(currentUser);
            }
        } else {
            for (int i = users.indexOf(currentUser); i < users.size() + users.indexOf(currentUser); i++) {
                supplyResourceToUser(users.get(i % users.size()), resultOfRoll);
            }
        }
        if (checkForVictory(sockets, currentUser)) {
            return;
        }
        gameState = GameState.USER_TURN;
        socketMessageSender.broadcastUserTurn(sockets, EventType.BROADCAST_USER_TURN);
        waitForTurnEnd(currentUser);
    }

    private void startTurn(List<Socket> sockets, User currentUser, Integer numOfTurn, EventType eventType) {
        socketMessageSender.broadcastPrepareUserTurn(sockets, currentUser.getId(), numOfTurn, eventType);
        currentUser.setHisTurn(true);
        currentUser.setIsReady(false);
    }

    private void randomStealResources(List<Socket> sockets) {
        for (User user : users) {
            long countOfResources = user.getResourceInventory().values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            if (countOfResources > 7) {
                List<Resource> resources = user.getResourceInventory().entrySet().stream()
                        .flatMap(entry -> Stream.generate(entry::getKey).limit(entry.getValue()))
                        .collect(toList());
                List<Resource> stolenResources = new ArrayList<>();
                Collections.shuffle(resources);
                for (int i = 0; i <= resources.size() / 2; i++) {
                    Resource stolenResource = resources.get(i);
                    stolenResources.add(stolenResource);
                    user.retrieveResourceFromInventory(stolenResource, 1);
                    bank.addResourceToStorage(stolenResource, 1);
                }
                socketMessageSender.broadcastRobberRobbery(sockets, user.getId(), stolenResources);
            }
        }
    }

    private void robberPlacement(User currentUser) {
        if (!currentUser.isHisTurn() || !gameState.equals(GameState.ROBBERY)) {
            return;
        }
        Hex robbedHex = hexes.stream().filter(Hex::isOccupiedByRobber)
                .findFirst()
                .orElse(null);

        if (robbedHex == null) {
            return;
        }

        List<User> usersInRobbedHex = robbedHex.getEdges().values().stream()
                .filter(edge -> edge.getUser() != null && edge.getUser() != currentUser)
                .map(Edge::getUser)
                .collect(toList());

        if (!usersInRobbedHex.isEmpty()) {
            Random random = new Random();
            Long victimId = usersInRobbedHex.get(random.nextInt(usersInRobbedHex.size())).getId();
            gameService.userRobbery(this, currentUser, victimId, robbedHex.getId());
        }
    }

    private void supplyResourceToUser(User user, int resultOfRoll) {
        Set<Hex> userHexes = getUserHexes(user);
        List<Resource> resources = collectResource(userHexes, resultOfRoll, user);
        List<Socket> sockets = getUsersSocket();
        bankService.supplyResourceToUser(bank, sockets, user, resources);
    }


    private boolean checkForVictory(List<Socket> sockets, User currentUser) {
        int victoryPoints = currentUser.getUserVictoryPoints(vertices);
        if (victoryPoints >= 10 && currentUser.isHisTurn()) {
            gameState = GameState.FINISHED;
            socketMessageSender.broadcastUserWin(sockets, currentUser.getId());
            return true;
        }
        return false;
    }

    private void waitForTurnEnd(User currentUser) {
        long turnEndTime = System.currentTimeMillis() + TURN_DURATION;
        while (System.currentTimeMillis() < turnEndTime && !currentUser.getIsReady()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private List<Resource> collectResource(Set<Hex> userHexes, int resultOfRoll, User user) {
        List<Resource> resources = new ArrayList<>();
        for (Hex hex : userHexes) {
            if (hex.getNumberToken() == resultOfRoll) {
                resources.addAll(getHexResources(hex, user));
            }
        }
        return resources;
    }

    private Collection<Resource> getHexResources(Hex hex, User user) {
        List<Resource> resources = new ArrayList<>();
        Resource hexResource = hex.getType().getResource();
        for (Vertex vertex : hex.getVertices().values()) {
            if (vertex.getUser() == null || !vertex.getUser().equals(user)) {
                continue;
            }
            if (vertex.getType().equals(VertexBuildingType.SETTLEMENT)) {
                resources.add(hexResource);
            } else if (vertex.getType().equals(VertexBuildingType.CITY)) {
                resources.add(hexResource);
                resources.add(hexResource);
            }
        }
        return resources;
    }

    private Set<Hex> getUserHexes(User user) {
        return hexes.stream()
                .filter(hex -> hex.getNumberToken() != null)
                .filter(hex -> hex.getVertices().values().stream()
                        .anyMatch(vertex -> vertex != null && vertex.getUser() != null && vertex.getUser().equals(user)))
                .collect(Collectors.toSet());
    }

    private void executePreparationTurnForUsers(
            List<Socket> sockets,
            Integer numOfTurn,
            EventType eventType,
            Function<User, List<?>> getUserElements,
            Consumer<User> assignRandomElementToUser
    ) {
        for (User currentUser : users) {
            startTurn(sockets, currentUser, numOfTurn, eventType);

            if (waitForUserAction(currentUser, getUserElements)) {
                assignRandomElementToUser.accept(currentUser);
            }
        }
    }

    private boolean waitForUserAction(User currentUser, Function<User, List<?>> getUserElements) {
        long turnEndTime = System.currentTimeMillis() + TURN_DURATION;
        while (System.currentTimeMillis() < turnEndTime || !currentUser.getIsReady()) {
            if (getUserElements.apply(currentUser).size() >= numOfTurn) {
                return false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        currentUser.setHisTurn(false);
        currentUser.setIsReady(false);
        return true;
    }

    private boolean waitForUserRobbery(User currentUser) {
        long turnEndTime = System.currentTimeMillis() + TURN_DURATION;
        while (!currentUser.getIsReady()) {
            if (System.currentTimeMillis() >= turnEndTime) {
                return false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        currentUser.setIsReady(false);
        return true;
    }

    private void assignRandomSettlementToUser(User user) {
        Vertex randomVertex = getRandomFreeElement(vertices, Vertex::getUser);
        if (randomVertex != null) {
            randomVertex.setType(VertexBuildingType.SETTLEMENT);
            randomVertex.setUser(user);
            user.setHisTurn(false);
            socketMessageSender.broadcastUserBuildSettlements(getUsersSocket(), user, randomVertex.getId());
        }
    }

    private void assignRandomRoadToUser(User user) {
        List<Edge> availableEdges = MapUtil.getAvailableEdgesForUser(user, vertices, edges);
        Edge randomEdge = getRandomFreeElement(availableEdges, Edge::getUser);
        if (randomEdge != null) {
            randomEdge.setType(EdgeBuildingType.ROAD);
            randomEdge.setUser(user);
            user.setHisTurn(false);
            socketMessageSender.broadcastUserBuildRoads(getUsersSocket(), user, randomEdge.getId());
        }
    }

    private <T> T getRandomFreeElement(List<T> elements, Function<T, User> getUserFunction) {
        long countOfFreeElements = elements.stream()
                .filter(element -> getUserFunction.apply(element) == null)
                .count();

        return elements.stream()
                .filter(element -> getUserFunction.apply(element) == null)
                .skip(ThreadLocalRandom.current().nextInt((int) countOfFreeElements))
                .findFirst()
                .orElse(null);
    }

    private void waitForUsersReady() throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            if (checkUsersReady()) {
                gameState = GameState.PREPARATION_BUILD_SETTLEMENTS;
                return;
            }
            log.info("Checking if users are ready (attempt {})", i + 1);
            Thread.sleep(1000);
        }
        cleanUpUnreadyUsers();
    }

    private List<Vertex> getUserVertices(User user) {
        return vertices.stream()
                .filter(vertex -> vertex.getUser() != null && vertex.getUser().equals(user))
                .toList();
    }

    private List<Edge> getUserEdges(User user) {
        return edges.stream()
                .filter(edge -> edge.getUser() != null && edge.getUser().equals(user))
                .toList();
    }

    private boolean checkUsersReady() {
        return users.stream().allMatch(User::getIsReady);
    }

    private void cleanUpUnreadyUsers() {
        users.removeIf(user -> {
            if (!user.getIsReady()) {
                try {
                    user.getSocket().close();
                } catch (IOException e) {
                    log.error("Error closing socket for unready user: {}", user.getId(), e);
                }
                return true;
            }
            return false;
        });
    }

    public List<Socket> getUsersSocket() {
        return users.stream()
                .map(User::getSocket)
                .filter(socket -> !socket.isClosed())
                .toList();
    }

    public User getUserById(Long userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    }
}