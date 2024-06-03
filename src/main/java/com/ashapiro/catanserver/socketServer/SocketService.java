package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;

import java.net.Socket;

public interface SocketService {
    <T extends SocketMessagePayload> void connectToLobby(Socket clientSocket, T message);

    <T extends SocketMessagePayload> void startGame(Socket clientSocket, T message);

    <T extends SocketMessagePayload> void buildHouse(Socket clientSocket, T message);

    void disconnectFromLobby(Socket clientSocket);

    void removeUserFromLobbyIfPresent(String token);

    void updateUserReadyStatus(Socket clientSocket);
}
