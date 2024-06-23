package com.ashapiro.catanserver.game.service;

import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Bank;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.game.model.Vertex;

import java.net.Socket;
import java.util.List;
import java.util.Map;

public interface BankService {

    void supplyResourceToUser(Bank bank, List<Socket> sockets, User user, List<Resource> resources);

    void transferResourceToBank(Bank bank, User user, Map<Resource, Integer> resourceCost);

    void tradeResource(Bank bank, User user, List<Vertex> userVertices, int requestedCountOfOutgoingResource, Resource incomingResource, Resource outgoingResource);

    Card buyCard(Bank bank, User user);

    boolean isBankContainsResources(
            Bank bank,
            Socket clientSocket,
            int desiredCountOfOutgoingResource,
            Resource outgoingResource
    );
}
