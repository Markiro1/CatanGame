package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestBuildPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestConnectPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestTradeResourcePayload;

import java.net.Socket;

public interface SocketService {

    void connectToLobby(Socket clientSocket, SocketRequestConnectPayload message);

    void startGame(Socket clientSocket, SocketRequestStartGamePayload message);

    void buildSettlement(Socket clientSocket, SocketRequestBuildPayload message);

    void buildRoad(Socket clientSocket, SocketRequestBuildPayload message);

    void buildCity(Socket clientSocket, SocketRequestBuildPayload message);

    void disconnectFromLobby(Socket clientSocket);

    void removeUserFromLobbyIfPresent(String token);

    void updateUserReadyStatus(Socket clientSocket);

    void tradeResource(Socket clientSocket, SocketRequestTradeResourcePayload socketMessage);
}
