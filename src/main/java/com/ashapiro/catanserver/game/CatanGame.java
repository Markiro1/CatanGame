package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.enums.EdgeBuildingType;
import com.ashapiro.catanserver.game.enums.GameState;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.enums.VertexBuildingType;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.Hex;
import com.ashapiro.catanserver.game.model.Vertex;
import com.ashapiro.catanserver.socketServer.SocketMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Getter
@Setter
@Slf4j
public class CatanGame extends Thread {

    private GameState gameState;

    private List<User> users;

    private List<Hex> hexes;

    private List<Edge> edges;

    private List<Vertex> vertices;

    private Bank bank;

    private SocketMessageSender socketMessageSender;

    private static final int TURN_DURATION = 60000;

    private Integer numOfTurn = 1;

    public CatanGame(List<User> users, List<Hex> hexes, List<Edge> edges, List<Vertex> vertices) {
        this.users = new ArrayList<>(users);
        this.bank = new BankImpl();
        this.hexes = hexes;
        this.edges = edges;
        this.vertices = vertices;
        gameState = GameState.WAITING;
        socketMessageSender = new SocketMessageSender(new ObjectMapper());
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
        List<Socket> sockets = getSocketsFromUsers();
        while (!gameState.equals(GameState.FINISHED)) {
            for (numOfTurn = 1; numOfTurn <= 1000; numOfTurn++) {
                for (User currentUser : users) {
                    executeTurn(sockets, currentUser);
                }
            }
            determineWinner(sockets);
        }
    }

    private void gamePreparation() {
        gameState = GameState.PREPARATION_BUILD_SETTLEMENTS;
        List<Socket> sockets = getSocketsFromUsers();
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

        gameState = GameState.STARTED;
    }

    private void executeTurn(List<Socket> sockets, User currentUser) throws InterruptedException {
        currentUser.setIsReady(false);
        startTurn(sockets, currentUser, numOfTurn, EventType.BROADCAST_USER_TURN);
        int resultOfRoll = diceRoll(currentUser, sockets);
        supplyResourceToUser(currentUser, resultOfRoll);
        waitForTurnEnd(currentUser);
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

    private void determineWinner(List<Socket> sockets) {
        if (!users.isEmpty()) {
            User randomUser = getRandomUser();
            if (randomUser != null) {
                socketMessageSender.broadcastUserWin(sockets, randomUser.getId());
                gameState = GameState.FINISHED;
            }
        }
    }

    private User getRandomUser() {
        Random random = new Random();
        int randomIndex = random.nextInt(users.size());
        return users.get(randomIndex);
    }

    private void supplyResourceToUser(User user, int resultOfRoll) {
        Set<Hex> userHexes =  getUserHexes(user);
        List<Resource> resources = collectResource(userHexes, resultOfRoll);
        List<Socket> sockets = getSocketsFromUsers();
        bank.supplyResourceToUser(socketMessageSender, sockets, user, resources);
    }

    private List<Resource> collectResource(Set<Hex> userHexes, int resultOfRoll) {
        List<Resource> resources = new ArrayList<>();
        for (Hex hex : userHexes) {
            if (hex.getNumberToken() == resultOfRoll) {
                resources.addAll(getHexResources(hex));
            }
        }
        return resources;
    }

    private Collection<Resource> getHexResources(Hex hex) {
        List<Resource> resources = new ArrayList<>();
        Resource hexResource = hex.getType().getResource();
        for (Vertex vertex : hex.getVertices().values()) {
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


    private int diceRoll(User user, List<Socket> sockets) {
        Random random = new Random();
        int firstRandomNumber = random.nextInt(6) + 1;
        int secondRandomNumber = random.nextInt(6) + 1;
        int resultOfRoll = firstRandomNumber + secondRandomNumber;
        socketMessageSender.broadcastUserDiceRoll(sockets, user, firstRandomNumber, secondRandomNumber);
        return resultOfRoll;
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
        }
        currentUser.setHisTurn(false);
        return true;
    }

    private void startTurn(List<Socket> sockets, User currentUser, Integer numOfTurn, EventType eventType) {
        socketMessageSender.broadcastUserTurn(sockets, currentUser.getId(), numOfTurn, eventType);
        currentUser.setHisTurn(true);
        currentUser.setIsReady(false);
    }

    private void assignRandomSettlementToUser(User user) {
        Vertex randomVertex = getRandomFreeElement(vertices, Vertex::getUser);
        if (randomVertex != null) {
            randomVertex.setType(VertexBuildingType.SETTLEMENT);
            randomVertex.setUser(user);
            user.setHisTurn(false);
            socketMessageSender.broadcastUserBuildSettlements(getSocketsFromUsers(), user, randomVertex.getId());
        }
    }

    private void assignRandomRoadToUser(User user) {
        List<Edge> availableEdges = MapUtil.getAvailableEdgesForUser(user, vertices, edges);
        Edge randomEdge = getRandomFreeElement(availableEdges, Edge::getUser);
        if (randomEdge != null) {
            randomEdge.setType(EdgeBuildingType.ROAD);
            randomEdge.setUser(user);
            user.setHisTurn(false);
            socketMessageSender.broadcastUserBuildRoads(getSocketsFromUsers(), user, randomEdge.getId());
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

    private List<Socket> getSocketsFromUsers() {
        return users.stream()
                .map(User::getSocket)
                .filter(socket -> !socket.isClosed())
                .toList();
    }

}
