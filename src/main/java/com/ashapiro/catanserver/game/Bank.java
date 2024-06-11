package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.SocketMessageSender;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestTradeResourcePayload;

import java.net.Socket;
import java.util.List;
import java.util.Map;

public interface Bank {

    void supplyResourceToUser(SocketMessageSender messageSender, List<Socket> sockets, User user, List<Resource> resources);

    Map<Resource, Integer> getStorage();

    void tradeResource(User user, SocketRequestTradeResourcePayload message);
}
