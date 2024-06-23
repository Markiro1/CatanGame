package com.ashapiro.catanserver.game.service;

import com.ashapiro.catanserver.game.CatanGame;
import com.ashapiro.catanserver.game.MapUtil;
import com.ashapiro.catanserver.game.ResourceCost;
import com.ashapiro.catanserver.game.enums.*;
import com.ashapiro.catanserver.game.model.*;
import com.ashapiro.catanserver.game.util.TransactionUtils;
import com.ashapiro.catanserver.socketServer.SocketMessageSenderImpl;

import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class GameServiceImpl implements GameService {

    private static GameServiceImpl instance;

    private BankService bankService;

    private UserService userService;

    private SocketMessageSenderImpl socketMessageSender;

    private List<Exchange> exchanges;

    public static synchronized GameServiceImpl getInstance() {
        if (instance == null) {
            instance = new GameServiceImpl();
            instance.socketMessageSender = SocketMessageSenderImpl.getInstance();
            instance.bankService = BankServiceImpl.getInstance();
            instance.userService = UserServiceImpl.getInstance();
            instance.exchanges = new ArrayList<>();
        }
        return instance;
    }

    @Override
    public void buildSettlement(Socket clientSocket, CatanGame game, User user, int fieldId) {
        Vertex currentVertex = game.getVertices().get(fieldId);

        boolean canBuildSettlement = canBuildSettlement(currentVertex, user, game);
        if (canBuildSettlement) {
            boolean isPreparationBuildSettlements = game.getGameState().equals(GameState.PREPARATION_BUILD_SETTLEMENTS);
            if (!isPreparationBuildSettlements) {
                bankService.transferResourceToBank(game.getBank(), user, ResourceCost.getSettlementCost());
            }
            Map<User, Long> edgeUserCounts = currentVertex.getNeighbourEdges().stream()
                    .filter(edge -> edge.getUser() != null && !edge.getUser().equals(user))
                    .collect(Collectors.groupingBy(Edge::getUser, Collectors.counting()));

            Optional<User> userWithTwoEdges = edgeUserCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() == 2)
                    .map(Map.Entry::getKey)
                    .findFirst();

            currentVertex.setUser(user);
            currentVertex.setType(VertexBuildingType.SETTLEMENT);
            socketMessageSender.broadcastUserBuildSettlements(game.getUsersSocket(), user, currentVertex.getId());
            if (userWithTwoEdges.isPresent()) {
                userService.updateUserLongestRoadAndCheckForLongestRoad(userWithTwoEdges.get(), game);
            }
            userService.updateUserLongestRoadAndCheckForLongestRoad(user, game);
        } else {
            socketMessageSender.sendMessage(clientSocket, "This vertex is already occupied");
        }
    }

    @Override
    public void buildRoad(Socket clientSocket, CatanGame game, User user, int fieldId) {
        Edge currentEdge = game.getEdges().get(fieldId);

        if (!canBuildRoad(currentEdge, user, game)) {
            socketMessageSender.sendMessage(clientSocket, "This edge is already occupied");
            return;
        }

        Map<Buff, Integer> userBuffs = user.getBuffs();
        Buff roadBuildingBuff = Buff.ROAD_BUILDING;

        boolean hasRoadBuildingBuff = userBuffs.containsKey(roadBuildingBuff) && userBuffs.get(roadBuildingBuff) > 0;
        boolean isPreparationBuildRoad = game.getGameState().equals(GameState.PREPARATION_BUILD_ROADS);

        if (!isPreparationBuildRoad) {
            if (hasRoadBuildingBuff) {
                user.removeBuff(roadBuildingBuff, 1);
            } else {
                bankService.transferResourceToBank(game.getBank(), user, ResourceCost.getRoadCost());
            }
        }

        currentEdge.setUser(user);
        currentEdge.setType(EdgeBuildingType.ROAD);
        socketMessageSender.broadcastUserBuildRoad(game.getUsersSocket(), user, currentEdge.getId());
        userService.updateUserLongestRoadAndCheckForLongestRoad(user, game);
    }

    @Override
    public void buildCity(Socket clientSocket, CatanGame game, User user, int fieldId) {
        Vertex currentVertex = game.getVertices().get(fieldId);

        if (canBuildCity(currentVertex, user, game)) {
            bankService.transferResourceToBank(game.getBank(), user, ResourceCost.getCityCost());
            currentVertex.setType(VertexBuildingType.CITY);
            socketMessageSender.broadcastUserBuildCity(game.getUsersSocket(), user, currentVertex.getId());
        }
    }

    @Override
    public void buyDevCard(CatanGame game, User user) {
        Bank bank = game.getBank();
        Card card = bankService.buyCard(bank, user);
        if (card == null) {
            socketMessageSender.sendMessage(user.getSocket(), "The bank ran out of cards");
        } else {
            bankService.transferResourceToBank(bank, user, ResourceCost.getCardCost());
            socketMessageSender.broadcastBuyCard(game.getUsersSocket(), user, card);
        }
    }

    @Override
    public void tradeResource(
            Socket clientSocket,
            CatanGame game,
            User user,
            int requestedAmountOfBuyResource,
            Resource sellResource,
            Resource buyResource
    ) {
        Bank bank = game.getBank();
        List<Socket> sockets = game.getUsersSocket();
        List<Vertex> userVertices = MapUtil.getUserVertices(user, game.getVertices());
        boolean userContainsResources = userService.isUserContainsResourceToTrade(user, userVertices, clientSocket, sellResource);
        boolean bankContainsResources = bankService.isBankContainsResources(bank, clientSocket, requestedAmountOfBuyResource, buyResource);
        if (bankContainsResources && userContainsResources) {
            bankService.tradeResource(bank, user, userVertices, requestedAmountOfBuyResource, sellResource, buyResource);
            socketMessageSender.broadcastUserTrade(sockets, user.getId(), sellResource, buyResource, requestedAmountOfBuyResource);
        }
    }

    @Override
    public void exchange(
            CatanGame game,
            Long exchangeId,
            boolean isAccept
    ) {
        Exchange exchange = exchanges.stream()
                .filter(e -> e.getExchangeId().equals(exchangeId))
                .findFirst()
                .orElse(null);
        if (exchange == null) return;

        if (!isAccept) {
            exchanges.remove(exchange);
            return;
        }

        processExchange(game, exchange);
    }

    private void processExchange(CatanGame game, Exchange exchange) {
        User initiatorUser = exchange.getInitiatorUser();
        User targetUser = exchange.getTargetUser();

        try {
            if (canExecuteExchange(initiatorUser, targetUser, exchange)) {
                executeExchange(game, exchange, initiatorUser, targetUser);
                exchanges.remove(exchange);
            }
        } catch (Exception e) {
            System.out.println("ERROR EXCHANGE RESOURCES BETWEEN USERS : " + e.getMessage());
        }
    }


    @Override
    public void exchangeResourcesOffer(
            CatanGame game,
            User initiatorUser,
            User targetUser,
            Resource initiatorResource,
            int initiatorAmountOfResource,
            Resource targetResource,
            int targetAmountOfResource
    ) {
        boolean isInitiatorContainsResources = userService.isUserContainsResourceToExchangeWithAnotherUser(
                initiatorUser,
                initiatorUser.getSocket(),
                initiatorResource,
                initiatorAmountOfResource
        );

        if (!isInitiatorContainsResources) {
            return;
        }

        Exchange exchange = new Exchange(
                initiatorUser,
                targetUser,
                targetAmountOfResource,
                initiatorAmountOfResource,
                initiatorResource,
                targetResource
        );
        exchanges.add(exchange);

        socketMessageSender.sendResponseExchangeOffer(
                targetUser.getSocket(),
                initiatorUser.getId(),
                targetAmountOfResource,
                initiatorAmountOfResource,
                initiatorResource,
                targetResource,
                exchange.getExchangeId()
        );
    }

    @Override
    public void userRobbery(CatanGame game, User robber, Long victimId, int hexId) {
        clearRobberFromHexes(game);

        Hex hex = getHexById(game, hexId);
        hex.setOccupiedByRobber(true);

        if (victimId != -1) {
            robVictim(game, robber, hex, victimId);
        }
        socketMessageSender.broadcastUserRobbery(game.getUsersSocket(), robber.getId(), -1L, hex.getId(), null);
        robber.setIsReady(true);
    }

    @Override
    public void useKnightCard(CatanGame game, User userWhoUseCard, int hexId) {
        boolean userIsAvailableUseCard = userService.userIsAvailableUseCard(userWhoUseCard, Card.KNIGHT);
        if (!userIsAvailableUseCard) {
            return;
        }
        clearRobberFromHexes(game);

        Hex hex = getHexById(game, hexId);
        hex.setOccupiedByRobber(true);

        userWhoUseCard.retrieveCardFromInventory(Card.KNIGHT, 1);
        userWhoUseCard.setUsedKnightCardCount(userWhoUseCard.getUsedKnightCardCount() + 1);
        List<Socket> sockets = game.getUsersSocket();
        socketMessageSender.broadcastUseKnightCard(sockets, userWhoUseCard.getId(), hexId);

        checkToTheLargestArmy(game);
    }

    @Override
    public void useMonopolyCard(CatanGame game, User userWhoUseCard, Resource resource) {
        boolean userIsAvailableUseCard = userService.userIsAvailableUseCard(userWhoUseCard, Card.MONOPOLY);
        if (!userIsAvailableUseCard) {
            return;
        }
        List<User> otherUsers = game.getUsers().stream()
                .filter(u -> !u.equals(userWhoUseCard))
                .toList();

        otherUsers.forEach(u -> {
            int amountOfResources = u.getResourceInventory().get(resource);
            if (amountOfResources > 0) {
                u.retrieveResourceFromInventory(resource, amountOfResources);
                userWhoUseCard.addResourceToInventory(resource, amountOfResources);
            }
        });
        userWhoUseCard.retrieveCardFromInventory(Card.MONOPOLY, 1);
        List<Socket> sockets = game.getUsersSocket();
        socketMessageSender.broadcastUseMonopolyCard(sockets, userWhoUseCard.getId(), resource);
    }

    @Override
    public void userYearOfPlentyCard(CatanGame game, User userWhoUseCard, List<Resource> resources) {
        boolean userIsAvailableUseCard = userService.userIsAvailableUseCard(userWhoUseCard, Card.YEAR_OF_PLENTY);
        if (!userIsAvailableUseCard) {
            return;
        }
        Bank bank = game.getBank();

        Map<Resource, Long> requestedResources = resources.stream()
                .collect(Collectors.groupingBy(resource -> resource, Collectors.counting()));

        for (Map.Entry<Resource, Long> entry : requestedResources.entrySet()) {
            Resource resource = entry.getKey();
            long count = entry.getValue();
            if (bank.getResourceStorage().getOrDefault(resource, 0) < count) {
                return;
            }
        }

        requestedResources.forEach((resource, amount) -> {
            bank.retrieveResourceFromStorage(resource, amount.intValue());
            userWhoUseCard.addResourceToInventory(resource, amount.intValue());
        });

        List<Socket> sockets = game.getUsersSocket();
        socketMessageSender.broadcastUseYearOfPlentyCard(sockets, userWhoUseCard.getId(), resources);
    }

    @Override
    public void useRoadBuildingCard(CatanGame game, User userWhoUseCard) {
        boolean userIsAvailableUseCard = userService.userIsAvailableUseCard(userWhoUseCard, Card.ROAD_BUILDING);
        if (!userIsAvailableUseCard) {
            return;
        }
        userWhoUseCard.addBuff(Buff.ROAD_BUILDING);

        List<Socket> sockets = game.getUsersSocket();
        socketMessageSender.broadcastUseRoadBuildingCard(sockets, userWhoUseCard.getId());
    }

    @Override
    public int diceRoll(User user, List<Socket> sockets) {
        Random random = new Random();
        int firstRandomNumber = random.nextInt(6) + 1;
        int secondRandomNumber = random.nextInt(6) + 1;
        int resultOfRoll = firstRandomNumber + secondRandomNumber;
        socketMessageSender.broadcastUserDiceRoll(sockets, user, firstRandomNumber, secondRandomNumber);
        return resultOfRoll;
    }

    private void executeExchange(CatanGame game, Exchange exchange, User initiatorUser, User targetUser) {
        List<Socket> sockets = game.getUsersSocket();

        TransactionUtils.executeWithRollback(initiatorUser, targetUser, () -> {
            updateUserResourceInventory(
                    initiatorUser,
                    exchange.getInitiatorResource(),
                    exchange.getInitiatorAmountOfResource(),
                    exchange.getTargetResource(),
                    exchange.getTargetAmountOfResource());

            updateUserResourceInventory(
                    targetUser,
                    exchange.getTargetResource(),
                    exchange.getTargetAmountOfResource(),
                    exchange.getInitiatorResource(),
                    exchange.getInitiatorAmountOfResource());
        });

        socketMessageSender.broadcastExchangeResources(
                sockets,
                initiatorUser.getId(),
                targetUser.getId(),
                exchange.getTargetAmountOfResource(),
                exchange.getInitiatorAmountOfResource(),
                exchange.getInitiatorResource(),
                exchange.getTargetResource()
        );
    }

    private void updateUserResourceInventory(User user, Resource removeResource, int removeAmount, Resource addResource, int addAmount) {
        user.retrieveResourceFromInventory(removeResource, removeAmount);
        user.addResourceToInventory(addResource, addAmount);
    }

    private boolean canExecuteExchange(User initiatorUser, User targetUser, Exchange exchange) {
        return userService.isUserContainsResourceToExchangeWithAnotherUser(
                initiatorUser, initiatorUser.getSocket(), exchange.getInitiatorResource(), exchange.getInitiatorAmountOfResource()
        ) && userService.isUserContainsResourceToExchangeWithAnotherUser(
                targetUser, targetUser.getSocket(), exchange.getTargetResource(), exchange.getTargetAmountOfResource()
        );
    }

    private void checkToTheLargestArmy(CatanGame game) {
        List<Socket> sockets = game.getUsersSocket();

        Optional<User> currentLargestArmyHolderOpt = game.getUsers().stream()
                .filter(User::isHaveLargestArmy)
                .findFirst();
        int currentUserLargestArmyUsedCard = currentLargestArmyHolderOpt.map(User::getUsedKnightCardCount).orElse(0);

        Optional<User> userWithMaxKnightsOpt = game.getUsers().stream()
                .max(Comparator.comparing(User::getUsedKnightCardCount));
        int userWithMaxKnightsUsedCard = userWithMaxKnightsOpt.map(User::getUsedKnightCardCount).orElse(0);

        if (userWithMaxKnightsUsedCard >= 3) {
            if (currentLargestArmyHolderOpt.isEmpty()) {
                userWithMaxKnightsOpt.ifPresent(user -> {
                    user.setHaveLargestArmy(true);
                });
            } else {
                User currentLargestArmyUser = currentLargestArmyHolderOpt.get();
                userWithMaxKnightsOpt.ifPresent(userWithMaxKnights -> {
                    if (!userWithMaxKnights.equals(currentLargestArmyUser) && currentUserLargestArmyUsedCard < userWithMaxKnightsUsedCard) {
                        currentLargestArmyUser.setHaveLargestArmy(false);
                        userWithMaxKnights.setHaveLargestArmy(true);
                        socketMessageSender.broadcastUserGetLargestArmy(sockets, userWithMaxKnights.getId());
                    }
                });
            }
        }
    }

    private boolean canBuildSettlement(Vertex currentVertex, User user, CatanGame game) {
        if (game.getGameState().equals(GameState.PREPARATION_BUILD_SETTLEMENTS)) {
            long countOfSettlements = game.getVertices().stream()
                    .filter(vertex -> vertex.getUser() != null && vertex.getUser().equals(user))
                    .count();
            return countOfSettlements < game.getNumOfTurn() && currentVertex.getType().equals(VertexBuildingType.NONE);
        }

        Map<Resource, Integer> settlementCost = ResourceCost.getSettlementCost();

        boolean hasRequiredResources = userService.isUserContainsResourcesToBuild(user, settlementCost);
        boolean isNoneVertex = currentVertex.getType().equals(VertexBuildingType.NONE);
        boolean isAvailableVertexForUser = isAvailableVertexForUser(currentVertex, user, game);

        return isNoneVertex && isAvailableVertexForUser && hasRequiredResources;
    }

    private boolean canBuildRoad(Edge currentEdge, User user, CatanGame game) {
        if (game.getGameState().equals(GameState.PREPARATION_BUILD_ROADS)) {
            long countOfRoads = game.getEdges().stream()
                    .filter(edge -> edge.getUser() != null && edge.getUser().equals(user))
                    .count();
            return countOfRoads < game.getNumOfTurn() && currentEdge.getType().equals(EdgeBuildingType.NONE);
        }

        Map<Resource, Integer> roadCost = ResourceCost.getRoadCost();
        boolean hasRequiredResources = userService.isUserContainsResourcesToBuild(user, roadCost);

        Map<Buff, Integer> userBuffs = user.getBuffs();
        Buff roadBuildingBuff = Buff.ROAD_BUILDING;

        boolean hasRoadBuildingBuff = userBuffs.containsKey(roadBuildingBuff) && userBuffs.get(roadBuildingBuff) > 0;
        boolean hasAvailableEdgeForUser = hasAvailableEdgeForUser(currentEdge, user, game);
        boolean isNoneEdge = currentEdge.getType().equals(EdgeBuildingType.NONE);

        return isNoneEdge && hasAvailableEdgeForUser && (hasRequiredResources || hasRoadBuildingBuff);
    }

    private boolean canBuildCity(Vertex currentVertex, User user, CatanGame game) {
        Map<Resource, Integer> cityCost = ResourceCost.getCityCost();

        boolean hasRequiresResources = userService.isUserContainsResourcesToBuild(user, cityCost);
        boolean hasAvailableVertexToBuildCity = hasAvailableVertexToBuildCity(currentVertex, user, game);
        boolean isSettlement = currentVertex.getType().equals(VertexBuildingType.SETTLEMENT);

        return isSettlement && hasAvailableVertexToBuildCity && hasRequiresResources;
    }

    private boolean isAvailableVertexForUser(Vertex currentVertex, User user, CatanGame game) {
        List<Edge> edges = game.getEdges();
        return MapUtil.getAvailableVerticesForUser(user, edges).contains(currentVertex);
    }

    private boolean hasAvailableEdgeForUser(Edge currentEdge, User user, CatanGame game) {
        List<Vertex> vertices = game.getVertices();
        List<Edge> edges = game.getEdges();
        return MapUtil.getAvailableEdgesForUser(user, vertices, edges).contains(currentEdge);
    }

    private boolean hasAvailableVertexToBuildCity(Vertex currentVertex, User user, CatanGame game) {
        return MapUtil.getUserVertices(user, game.getVertices()).contains(currentVertex);
    }

    private void clearRobberFromHexes(CatanGame game) {
        game.getHexes().stream().forEach(hex -> hex.setOccupiedByRobber(false));
    }

    private Hex getHexById(CatanGame game, int hexId) {
        return game.getHexes().stream()
                .filter(hex -> hex.getId().equals(hexId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Hex not found with id: " + hexId));
    }

    private void robVictim(CatanGame game, User robber, Hex hex, Long victimId) {
        User victim = game.getUserById(victimId);
        if (victim == null || victim.equals(robber)) {
            return;
        }
        if (!isVictimOnHexEdges(hex, victim)) {
            return;
        }
        Resource stolenResource = stealResourceFromVictim(robber, victim);
        socketMessageSender.broadcastUserRobbery(game.getUsersSocket(), robber.getId(), victimId, hex.getId(), stolenResource);
    }

    private Resource stealResourceFromVictim(User robber, User victim) {
        List<Resource> resources = victim.getResourceInventory().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();
        Resource stolenResource = null;
        if (!resources.isEmpty()) {
            stolenResource = resources.get(new Random().nextInt(resources.size()));
            Resource finalStolenResource = stolenResource;
            TransactionUtils.executeWithRollback(robber, victim, () -> {
                robber.stealFromAnotherUser(victim, finalStolenResource);
            });
        }
        return stolenResource;

      /* todo: PREVIOUS VERSION
      List<Resource> resources = victim.getResourceInventory().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();
        if (!resources.isEmpty()) {
            Resource stolenResource = resources.get(new Random().nextInt(resources.size()));
            robber.stealFromAnotherUser(victim, stolenResource);
            return stolenResource;
        }
        return null;*/
    }

    private boolean isVictimOnHexEdges(Hex hex, User victim) {
        return hex.getEdges().values().stream()
                .anyMatch(edge -> edge.getUser() != null && edge.getUser().equals(victim));
    }

}
