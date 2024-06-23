package com.ashapiro.catanserver.game.service;

import com.ashapiro.catanserver.game.CatanGame;
import com.ashapiro.catanserver.game.MapUtil;
import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.HarborType;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.game.model.Vertex;
import com.ashapiro.catanserver.socketServer.SocketMessageSender;
import com.ashapiro.catanserver.socketServer.SocketMessageSenderImpl;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {

    private static UserServiceImpl instance;

    private SocketMessageSender socketMessageSender;

    public static synchronized UserServiceImpl getInstance() {
        if (instance == null) {
            instance = new UserServiceImpl();
            instance.socketMessageSender = SocketMessageSenderImpl.getInstance();
        }
        return instance;
    }

    @Override
    public boolean userIsAvailableUseCard(User userWhoUseCard, Card card) {
        return userWhoUseCard.isHisTurn() && userWhoUseCard.containsCard(card);
    }

    @Override
    public int getLongestUserRoadLength(User user, List<Edge> edges) {
        List<Edge> userEdges = edges.stream()
                .filter(edge -> edge.getUser() != null && edge.getUser().equals(user))
                .collect(Collectors.toList());

        int maxNum = 0;
        for (Edge userEdge : userEdges) {
            int currentNum = findLongestRoadLength(userEdge, null, new ArrayList<>(), user);
            maxNum = Math.max(maxNum, currentNum);
        }

        return maxNum;
    }

    @Override
    public boolean isUserContainsResourcesToBuild(User user, Map<Resource, Integer> cost) {
        return cost.entrySet().stream()
                .allMatch(entry -> user.getResourceInventory().getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    @Override
    public boolean isUserContainsResourceToTrade(User user, List<Vertex> userVertices, Socket clientSocket, Resource sellResource) {
        Map<Resource, Integer> userInventory = user.getResourceInventory();
        int availableUserResources = userInventory.get(sellResource);
        boolean isUserContainsResources;
        if (MapUtil.isUserHaveSpecialHarborWithType(userVertices, HarborType.getTypeByResource(sellResource))) {
            isUserContainsResources = availableUserResources >= 2;
        } else if (MapUtil.isUserHaveGenericHarbor(userVertices)) {
            isUserContainsResources = availableUserResources >= 3;
        } else {
            isUserContainsResources = availableUserResources >= 4;
        }
        if (!isUserContainsResources) {
            socketMessageSender.sendMessage(clientSocket,
                    String.format("%s does not have enough resources with type ^%s", user.getName(), sellResource));
        }
        return isUserContainsResources;
    }

    @Override
    public boolean isUserContainsResourceToExchangeWithAnotherUser(User user, Socket clientSocket, Resource resource, int amountOfResource) {
        Map<Resource, Integer> userInventory = user.getResourceInventory();
        int availableUserResources = userInventory.getOrDefault(resource, 0);
        boolean isUserContainsResources = availableUserResources >= amountOfResource;
        if (!isUserContainsResources) {
            socketMessageSender.sendMessage(clientSocket,
                    String.format("%s does not have enough resources with type ^%s", user.getName(), resource));
        }
        return isUserContainsResources;
    }

    @Override
    public void updateUserLongestRoadAndCheckForLongestRoad(User user, CatanGame game) {
        int userLongestRoad = getLongestUserRoadLength(user, game.getEdges());
        user.setLongestRoadLength(userLongestRoad);
        updateLongestRoadStatusIfNeeded(game);
    }

    private User findUserWithMaxRoads(List<User> users) {
        return users.stream()
                .max(Comparator.comparingInt(User::getLongestRoadLength))
                .orElse(null);
    }


    private User findUserWithLongestRoad(List<User> users) {
        return users.stream()
                .filter(User::isHaveLongestRoad)
                .findFirst()
                .orElse(null);
    }

    private void updateLongestRoadStatusIfNeeded(CatanGame game) {
        List<User> users = game.getUsers();
        List<Socket> sockets = game.getUsersSocket();

        User currentLongestRoadUser = findUserWithLongestRoad(users);
        User userWithMaxRoads = findUserWithMaxRoads(users);

        if (userWithMaxRoads != null && userWithMaxRoads.getLongestRoadLength() >= 5) {
            boolean isShouldUpdate = shouldUpdateLongestRoadOwner(currentLongestRoadUser, userWithMaxRoads);
            if (isShouldUpdate) {
                updateLongestRoadOwner(sockets, currentLongestRoadUser, userWithMaxRoads);
            }
        }
    }

    private void updateLongestRoadOwner(List<Socket> sockets, User previousOwner, User newOwner) {
        if (previousOwner != null) {
            previousOwner.setHaveLongestRoad(false);
        }
        newOwner.setHaveLongestRoad(true);
        socketMessageSender.broadcastUserGetLongestRoad(sockets, newOwner.getId());
    }

    private boolean shouldUpdateLongestRoadOwner(User currentLongestRoadUser, User userWithMaxRoads) {
        return !userWithMaxRoads.equals(currentLongestRoadUser) &&
                (currentLongestRoadUser == null || userWithMaxRoads.getLongestRoadLength() > currentLongestRoadUser.getLongestRoadLength());
    }

    private int findLongestRoadLength(Edge currentEdge, Edge previousEdge, ArrayList<Edge> currentRoad, User user) {
        currentRoad.add(currentEdge);
        int maxLength = currentRoad.size();

        List<Vertex> nextVertices = new ArrayList<>(currentEdge.getNeighborVertices());

        if (previousEdge != null) {
            nextVertices.removeAll(previousEdge.getNeighborVertices());
        }

        List<Edge> nextUserEdges = nextVertices.stream()
                .filter(vertex -> vertex.getUser() == null || vertex.getUser().equals(user))
                .flatMap(vertex -> vertex.getNeighbourEdges().stream())
                .filter(edge -> edge.getUser() != null && edge.getUser().equals(user) && !edge.equals(currentEdge))
                .collect(Collectors.toList());
        ;

        for (Edge nextUserEdge : nextUserEdges) {
            int currentRoadLength = findLongestRoadLength(nextUserEdge, currentEdge, new ArrayList<>(currentRoad), user);
            maxLength = Math.max(maxLength, currentRoadLength);
        }

        return maxLength;
    }
}
