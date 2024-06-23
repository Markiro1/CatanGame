package com.ashapiro.catanserver.game.service;

import com.ashapiro.catanserver.game.CatanGame;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.User;

import java.net.Socket;
import java.util.List;

public interface GameService {

    void buildSettlement(Socket clientSocket, CatanGame game, User user, int fieldId);

    void buildRoad(Socket clientSocket, CatanGame game, User user, int fieldId);

    void buildCity(Socket clientSocket, CatanGame game, User user, int fieldId);

    void tradeResource(
            Socket clientSocket,
            CatanGame game,
            User user,
            int requestedAmountOfBuyResource,
            Resource sellResource,
            Resource buyResource
    );

    void exchangeResourcesOffer(
            CatanGame game,
            User initiatorUser,
            User targetUser,
            Resource initiatorResource,
            int initiatorAmountOfResource,
            Resource targetResource,
            int targetAmountOfResource
    );

    void exchange(
            CatanGame game,
            Long exchangeId,
            boolean isAccept
    );

    void buyDevCard(CatanGame game, User user);

    void userRobbery(CatanGame game, User robber, Long victimUserId, int hexId);

    void useKnightCard(CatanGame game, User userWhoUseCard, int hexId);

    void useMonopolyCard(CatanGame game, User userWhoUseCard, Resource resource);

    void userYearOfPlentyCard(CatanGame game, User userWhoUseCard, List<Resource> resources);

    void useRoadBuildingCard(CatanGame game, User userWhoUseCard);

    int diceRoll(User user, List<Socket> sockets);
}
