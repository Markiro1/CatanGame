package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.request.*;

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

    void exchangeResourcesOffer(Socket clientSocket, SocketRequestExchangeResourcesOfferPayload socketMessage);

    void buyCard(Socket clientSocket);

    void userRobbery(Socket clientSocket, SocketRequestRobberyPayload message);

    void useKnightCard(Socket clientSocket, SocketRequestUseKnightCardPayload message);

    void useMonopolyCard(Socket clientSocket, SocketRequestUseMonopolyCardPayload message);

    void useYearOfPlentyCard(Socket clientSocket, SocketRequestUseYearOfPlentyCardPayload message);

    void useRoadBuildingCard(Socket clientSocket);

    void exchange(Socket clientSocket, SocketRequestExchangePayload socketMessage);
}
