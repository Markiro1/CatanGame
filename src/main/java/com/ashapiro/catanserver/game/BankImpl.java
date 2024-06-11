package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.SocketMessageSender;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestTradeResourcePayload;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BankImpl implements Bank {

    private Map<Resource, Integer> storage;

    public BankImpl() {
        this.storage = new ConcurrentHashMap<>();
        initializeStorage(this.storage);
    }

    @Override
    public void supplyResourceToUser(SocketMessageSender messageSender, List<Socket> sockets, User user, List<Resource> resources) {
        if (resources.isEmpty()) {
            return;
        }
        for (Resource resource : resources) {
            int availableResource = storage.getOrDefault(resource, 0);

            if (availableResource <= 0) {
                messageSender.sendMessage(user.getSocket(), "The bank ran out of resources");
            } else {
                Map<Resource, Integer> userInventory = user.getInventory();
                userInventory.put(resource, userInventory.getOrDefault(resource, 0) + 1);
                storage.put(resource, availableResource - 1);
            }
        }
        messageSender.broadcastUserGetResource(sockets, user, resources);
    }

    @Override
    public void tradeResource(User user, SocketRequestTradeResourcePayload message) {
        int requestedCountOfOutgoingResource = message.getRequestedCountOfOutgoingResource();
        Resource incomingResource = message.getIncomingResource();
        Resource outgoingResource = message.getOutgoingResource();
        user.getInventory().put(incomingResource, user.getInventory().getOrDefault(incomingResource, 0) - 4);
        storage.put(incomingResource, storage.getOrDefault(incomingResource, 0) + 4);
        storage.put(outgoingResource, storage.getOrDefault(outgoingResource, 0) - requestedCountOfOutgoingResource);
        user.getInventory().put(outgoingResource, user.getInventory().getOrDefault(outgoingResource, 0) + requestedCountOfOutgoingResource);
    }

    @Override
    public Map<Resource, Integer> getStorage() {
        return storage;
    }

    private void initializeStorage(Map<Resource, Integer> storage) {
        for (Resource resource : Resource.values()) {
            for (int i = 1; i <= 20; i++) {
                storage.put(resource, i);
            }
        }
    }
}
