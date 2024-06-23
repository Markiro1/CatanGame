package com.ashapiro.catanserver.game.service;

import com.ashapiro.catanserver.game.CatanGame;
import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.game.model.Vertex;

import java.net.Socket;
import java.util.List;
import java.util.Map;

public interface UserService {
    int getLongestUserRoadLength(User user, List<Edge> edges);

    void updateUserLongestRoadAndCheckForLongestRoad(User user, CatanGame game);

    boolean userIsAvailableUseCard(User userWhoUseCard, Card card);

    boolean isUserContainsResourcesToBuild(User user, Map<Resource, Integer> cost);

    boolean isUserContainsResourceToTrade(User user, List<Vertex> userVertices, Socket clientSocket, Resource sellResource);

    boolean isUserContainsResourceToExchangeWithAnotherUser(User user, Socket clientSocket, Resource givenResource, int amountOfResource);
}
