package com.ashapiro.catanserver.game.service;

import com.ashapiro.catanserver.game.MapUtil;
import com.ashapiro.catanserver.game.enums.Card;
import com.ashapiro.catanserver.game.enums.HarborType;
import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.game.model.Bank;
import com.ashapiro.catanserver.game.model.User;
import com.ashapiro.catanserver.game.model.Vertex;
import com.ashapiro.catanserver.game.util.TransactionUtils;
import com.ashapiro.catanserver.socketServer.SocketMessageSenderImpl;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BankServiceImpl implements BankService {

    private static BankServiceImpl instance;

    private SocketMessageSenderImpl socketMessageSender;

    public static synchronized BankServiceImpl getInstance() {
        if (instance == null) {
            instance = new BankServiceImpl();
            instance.socketMessageSender = SocketMessageSenderImpl.getInstance();
        }
        return instance;
    }

    @Override
    public void supplyResourceToUser(Bank bank, List<Socket> sockets, User user, List<Resource> resources) {
        if (resources.isEmpty()) {
            return;
        }
        TransactionUtils.executeWithRollback(user, bank, () -> {
            for (int i = 0; i < resources.size(); i++) {
                Resource resource = resources.get(i);
                int availableResource = bank.getResourceStorage().getOrDefault(resource, 0);

                if (availableResource <= 0) {
                    resources.remove(resource);
                    i--;
                    socketMessageSender.sendMessage(user.getSocket(), "The bank ran out of resources");
                } else {
                    user.addResourceToInventory(resource, 1);
                    bank.retrieveResourceFromStorage(resource, 1);
                }
            }
            socketMessageSender.broadcastUserGetResource(sockets, user, resources);
        });
    }

    @Override
    public void tradeResource(Bank bank, User user,
                              List<Vertex> userVertices,
                              int requestedAmountOfBuyResource,
                              Resource sellResource,
                              Resource buyResource
    ) {
        TransactionUtils.executeWithRollback(user, bank, () -> {
            int incomingAmount;
            if (MapUtil.isUserHaveSpecialHarborWithType(userVertices, HarborType.           getTypeByResource(sellResource))) {
                incomingAmount = 2 * requestedAmountOfBuyResource;
            } else if (MapUtil.isUserHaveGenericHarbor(userVertices)) {
                incomingAmount = 3 * requestedAmountOfBuyResource;
            } else {
                incomingAmount = 4 * requestedAmountOfBuyResource;
            }
            user.retrieveResourceFromInventory(sellResource, incomingAmount);
            user.addResourceToInventory(buyResource, requestedAmountOfBuyResource);
            bank.addResourceToStorage(sellResource, incomingAmount);
            bank.retrieveResourceFromStorage(buyResource, requestedAmountOfBuyResource);
        });
    }

    @Override
    public Card buyCard(Bank bank, User user) {
        List<Card> availableCards = bank.getCardStorage().entrySet().stream()
                .flatMap(entry -> Stream.generate(entry::getKey).limit(entry.getValue()))
                .collect(Collectors.toList());
        Card[] randomCard = {availableCards.get(new Random().nextInt(availableCards.size()))};
        TransactionUtils.executeWithRollback(user, bank, card -> {
            if (bank.retrieveCardFromStorage(randomCard[0], 1)) {
                user.addCardToInventory(randomCard[0], 1);
            } else {
                randomCard[0] = null;
            }
        });
        return randomCard[0];
    }

    @Override
    public void  transferResourceToBank(Bank bank, User user, Map<Resource, Integer> resourceCost) {
        TransactionUtils.executeWithRollback(user, bank, () -> {
            for (Map.Entry<Resource, Integer> entry : resourceCost.entrySet()) {
                Resource resource = entry.getKey();
                int cost = entry.getValue();
                user.retrieveResourceFromInventory(resource, cost);
                bank.addResourceToStorage(resource, cost);
            }
        });
    }

    @Override
    public boolean isBankContainsResources(Bank bank, Socket clientSocket, int desiredCountOfOutgoingResource, Resource outgoingResource) {
        int availableResource = bank.getResourceStorage().getOrDefault(outgoingResource, 0);
        boolean bankContainsResources = availableResource >= desiredCountOfOutgoingResource;
        if (!bankContainsResources) {
            socketMessageSender.sendMessage(clientSocket,
                    String.format("The bank not have enough resource with type %s in storage", outgoingResource));
        }
        return bankContainsResources;
    }
}
